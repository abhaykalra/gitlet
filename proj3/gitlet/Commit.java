package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
/** @author Abhay Kalra a. */
public class Commit implements Serializable {
    /** @return a. */
    private String _timestamp;
    /** @return a. */
    private String _message;
    /** @return a. */
    private final HashMap<String, String> _fileToID;
    /** @return a. */
    private String _ID;
    /** @return a. */
    private String _parent;
    /**  a. */
    public Commit() {
        _fileToID = new HashMap<String, String>();
        _ID = createID();
        _parent = "";
        _message = "";
        _timestamp = "";
    }
    /**  @param firstCommit a. */
    public Commit(boolean firstCommit) {
        this();
        if (firstCommit) {
            _timestamp = String.format("%1$ta %1$tb %1$td %1$tT %1$tY %1$tz",
                    new Date(0));
            _ID = Utils.sha1(_timestamp);
            _message = "initial commit";
        }
    }
    /** a. */
    public void createTimestamp() {
        Date currentDate = new Date(System.currentTimeMillis());
        _timestamp = String.format("%1$ta %1$tb %1$td %1$tT %1$tY %1$tz",
                currentDate);
        _ID = createID();
    }
    /**  @return a. */
    private String createID() {
        List<String> idsList = new ArrayList<String>(_fileToID.values());
        Collections.sort(idsList);
        idsList.add(_timestamp);
        idsList.add(_message);
        return Utils.sha1(idsList.toString());
    }
    /**  @return a. */
    public String getID() {
        return _ID;
    }
    /**  @return a. */
    public String getMessage() {
        return _message;
    }
    /** @param file a
     * @return a. */
    public String getIDFromFile(String file) {
        if (!_fileToID.containsKey(file)) {
            return "";
        }
        return _fileToID.get(file);
    }
    /** @param id a.
     * @return string a.*/
    public String getFileFromID(String id) {
        for (String filePath: _fileToID.keySet()) {
            if (_fileToID.get(filePath).equals(id)) {
                return filePath;
            }
        }
        return null;
    }
    /** @return a. */
    public HashMap<String, String> getFileToID() {
        return _fileToID;
    }
    /** @return a. */
    public String getParent() {
        return _parent;
    }
    /** @return a. */
    public String getTimestamp() {
        return _timestamp;
    }
    /** @param f a. */
    public void addFile(File f) throws IOException {
        String id = Utils.sha1(Utils.readContents(f));
        _fileToID.put(f.getCanonicalPath(), id);
        _ID = createID();
    }
    /** @param f a. */
    public void addRemoveFile(File f) throws IOException {
        _fileToID.put(f.getCanonicalPath(), "remove*" + f.getName());
        _ID = createID();
    }
    /** @param id a. */
    public void setParent(String id) {
        _parent = id;
    }
    /** @param message a. */
    public void setMessage(String message) {
        _message = message;
    }


}

