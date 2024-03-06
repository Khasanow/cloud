package com.example.cloud.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@Entity
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private int size;
    private boolean itsRemoved;

    String userLogin;

    public File(String fileName, int size, String userLogin) {
        this.fileName = fileName;
        this.size = size;
        this.userLogin = userLogin;
        itsRemoved = false;
    }
}
