package com.ambition.user.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Elewin
 * 2019-05-15 11:12 PM
 */
@Data
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = -2013584012038496607L;

    private Integer id;

    private String name;

    private String password;

    private Integer status;

    private Integer active;

    private Date createdTime;

    private Date updatedTime;
}
