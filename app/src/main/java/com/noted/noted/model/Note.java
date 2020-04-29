package com.noted.noted.model;

import com.noted.noted.R;

import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@Parcel(
        implementations = {io.realm.com_noted_noted_model_NoteRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze =  {Note.class} )
public class Note extends RealmObject {
    @PrimaryKey
    public long id = 0;
    public String title = "";
    public String body = "";
    public long date = 0;
    public int color = R.color.background;
    public RealmList<NoteCategory> categories = new RealmList<>();

    public Note(){}

    public Note(long id, String title, String body, long date, int color, RealmList<NoteCategory> categories) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.date = date;
        this.color = color;
        this.categories = categories;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public RealmList<NoteCategory> getCategories() {
        return categories;
    }

    @ParcelPropertyConverter(CategoryListParcelConverter.class)
    public void setCategories(RealmList<NoteCategory> categories) {
        this.categories = categories;
    }
}
