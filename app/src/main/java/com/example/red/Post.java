package com.example.red;

import java.util.HashMap;
import java.util.Map;

public class Post {
        public String uid;
        public String author;
        public String authorPhotoUrl;
        public String content;
        public String mediaUrl;
        public String mediaType;
        public Map<String, Boolean> likes = new HashMap<>();

        // Constructor vacio requerido por Firestore

        public Post() {}

        public Post(String uid, String author, String authorPhotoUrl, String content, String mediaUrl, String mediaType) {
            this.uid = uid;
            this.author = author;
            this.authorPhotoUrl = authorPhotoUrl;
            this.content = content;
            this.mediaUrl = mediaUrl;
            this.mediaType = mediaType;
        }

    }