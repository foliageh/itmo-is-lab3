package com.twillice.itmoislab1.service;

import io.minio.*;
import jakarta.ejb.Stateless;

import java.io.InputStream;

@Stateless
public class MinioService {
    private final MinioClient minioClient;

    public MinioService() {
        minioClient = io.minio.MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("minioadmin", "minioadmin")
                .build();
        try {
            if (!minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket("temp-imports").build()))
                minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket("temp-imports").build());
            if (!minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket("imports").build()))
                minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket("imports").build());
        } catch (Exception e) {
            System.err.println("Failed to ensure MinIO buckets exist");
        }
    }

    public void uploadFile(String bucket, String fileName, InputStream fileInputStream) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileName)
                        .stream(fileInputStream, fileInputStream.available(), -1)
                        .contentType("application/json")
                        .build()
        );
    }

    public void copyFile(String sourceBucket, String destinationBucket, String fileName) throws Exception {
//        throw new Exception("upload to minio oops");
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(destinationBucket)
                        .object(fileName)
                        .source(CopySource.builder()
                                .bucket(sourceBucket)
                                .object(fileName)
                                .build())
                        .build()
        );
    }

    public void deleteFile(String bucket, String fileName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .build()
        );
    }

    public InputStream getFile(String bucket, String fileName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .build()
        );
    }
}