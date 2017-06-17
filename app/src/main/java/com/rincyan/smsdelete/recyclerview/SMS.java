package com.rincyan.smsdelete.recyclerview;

/**
 * Created by rin on 2017/6/15.
 *
 */

public class SMS {
    private String num;
    private String body;
    private String date;
    private Long _id;

    public SMS(String num, String body, String date, Long id) {
        this.num = num;
        this.body = body;
        this.date = date;
        this._id = id;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNum() {
        return this.num;
    }

    public String getBody() {
        return this.body;
    }

    public String getDate() {
        return this.date;
    }

    public Long getId() {
        return this._id;
    }
}
