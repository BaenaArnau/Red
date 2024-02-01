package com.example.red;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegisterFragment extends Fragment {
    private EditText emailEditText, passwordEditText;
    NavController navController;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        registerButton = view.findViewById(R.id.registerButton);
        ImageView profileImageView = view.findViewById(R.id.profileImageView);
        Button choosePhotoButton = view.findViewById(R.id.choosePhotoButton);

        choosePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearCuenta();
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ImageView profileImageView = requireView().findViewById(R.id.profileImageView);
            profileImageView.setImageURI(selectedImageUri);
        }
    }

    private void crearCuenta() {
        if (!validarFormulario()) {
            return;
        }

        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Usuario creado exitosamente
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                actualizarUI(uid);
                                // Subir la foto solo si se seleccionó una
                                if (selectedImageUri != null) {
                                    subirFoto(selectedImageUri,emailEditText.getText().toString());
                                }
                            }
                        } else {
                            // Manejar el error
                            Snackbar.make(requireView(), "Error: " + task.getException(), Snackbar.LENGTH_LONG).show();
                            registerButton.setEnabled(true);
                        }
                    }
                });
    }

    private void subirFoto(Uri imageUri, String userEmail) {
        // Utilizar el correo del usuario como nombre de la imagen
        String imageFileName = userEmail.replace("@", "_").replace(".", "_");

        // Obtén la referencia al archivo en Firebase Storage
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profiles/" + imageFileName);

        // Sube la imagen al almacenamiento
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Imagen cargada exitosamente, ahora obtén la URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String photoUrl = uri.toString();
                        // Guardar la URL en tu base de datos o donde sea necesario.
                        guardarUrlEnBaseDeDatos(photoUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    // Manejar errores
                    Snackbar.make(requireView(), "Error al cargar la foto: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private void guardarUrlEnBaseDeDatos(String photoUrl) {
        // Obtener el UID del usuario actual
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Guardar la URL de la foto en la base de datos (por ejemplo, Firebase Realtime Database o Firestore)

        // Ejemplo con Firebase Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        databaseRef.child("photoUrl").setValue(photoUrl)
                .addOnSuccessListener(aVoid -> {
                    // La URL de la foto se ha guardado correctamente en la base de datos.
                    registerButton.setEnabled(true);
                    // Redirigir a la pantalla principal
                    navController.navigate(R.id.homeFragment);
                })
                .addOnFailureListener(e -> {
                    // Manejar errores al guardar la URL en la base de datos.
                    Snackbar.make(requireView(), "Error al guardar la URL de la foto en la base de datos: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    registerButton.setEnabled(true);
                });
    }


    private void actualizarUI(String currentUser) {
        if(currentUser != null){
            navController.navigate(R.id.homeFragment);
        }
    }

    private boolean validarFormulario() {
        boolean valid = true;

        if (TextUtils.isEmpty(emailEditText.getText().toString())) {
            emailEditText.setError("Required.");
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        if (TextUtils.isEmpty(passwordEditText.getText().toString())) {
            passwordEditText.setError("Required.");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }
}