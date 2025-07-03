package fr.abes.item.core.exception;

import fr.abes.item.core.constant.Constant;

public class FileLineDATWithTitleTooLongException extends FileLineException {
    public FileLineDATWithTitleTooLongException(){
        super(Constant.ERR_FILE_WRONGCONTENT_DATA_WITHTITLETOOLONG);
    }
}
