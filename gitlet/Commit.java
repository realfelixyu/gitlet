import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {

    /** Unique ID of a commit from SHA-1 hash.*/
    private String sha_id;
    /** ArrayList of SHA-1 hash of this commit’s parent(s) in case of merge.*/
    private ArrayList<String> parents;
    /** Date when this commit was made. */
    private Date commitDate;
    /** Mapping from file names to their SHA IDs*/
    private HashMap<String, String> blobs;
    /** The message of the commit. */
    private String message;

    public Commit() {
        blobs = new HashMap<>();
        parents = new ArrayList<>();
    }

    /** Set the commit date of this commit to unix epoch time 0. */
    public void setFirstCommitDate() {
        commitDate = new Date(0);
    }

    /** Adds files in staging directory to be saved.
     * Sets date of commit and clears staging directory.*/
    public void actCommit() throws IOException {
        String[] fileNames = Main.stagingDir.list();
        //set time
        this.commitDate = new Date(System.currentTimeMillis());
        //read contents
        for (String file : fileNames) {
            byte[] stream = Utils.readContents(Utils.join(Main.stagingDir, file));
            String fileID = Utils.sha1(stream);
            blobs.put(file, fileID);
            File target = Utils.join(Main.blobs, fileID);
            File current = Utils.join(Main.stagingDir, file);
            if (!target.exists()) {
                Files.copy(current.toPath(), target.toPath());
            }
        }
    }

    public boolean sameContent(File file1, File file2) {
        String string1 = Utils.readContentsAsString(file1);
        String string2 = Utils.readContentsAsString(file2);
        if (Utils.sha1(string1).equals(Utils.sha1(string2))) {
            return true;
        }
        return false;
    }

    /** Clears the staging area after a commit.*/
    public void clearStage() {
        String[] stagingFileNames = Main.stagingDir.list();
        for (int i = 0; i < stagingFileNames.length; i++) {
            File temp = Utils.join(Main.stagingDir, stagingFileNames[i]);
            temp.delete();
        }
    }

    /** @return list of strings for hashing this commit. */
    public String[] returnHashList() {
        String[] returnList = new String[4];
        returnList[0] = parents.get(0);
        if (parents.size() > 1) {
            returnList[1] = parents.get(1);
        }
        returnList[2] = commitDate.toString();
        returnList[3] = message;
        return returnList;
    }

    /** @return a string representation of this object. */
    public String toString() {
        return "";
    }

    /** A setter method for this commit's PARENT. */
    public void addParent(String parent) {
        parents.add(parent);
    }

    /** A setter method for this commit's ID. */
    public void setSha_id(String id) {
        sha_id = id;
    }

    public String getSHA_ID() {
        return sha_id;
    }

    public ArrayList<String> getParent() {
        return parents;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
