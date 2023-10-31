package com.vctek.orderservice.model;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
public class ItemModel implements Serializable {
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
