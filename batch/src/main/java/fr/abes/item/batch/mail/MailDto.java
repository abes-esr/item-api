package fr.abes.item.batch.mail;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MailDto {
    private String app;
    private String[] to;
    private String[] cc;
    private String[] cci;
    private String subject;
    private String text;
}
