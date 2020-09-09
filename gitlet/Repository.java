package gitlet;

import edu.neu.ccs.util.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Formatter;
import java.util.Date;

public class Repository implements Serializable {

    /** A map from commits’ IDs to the commit objects. */
    private HashMap<String, Commit> commits;

    /** The SHA-1 hash id of the current commit. */
    private String head;

    /** A map from branch name to its current commit SHA-1 hash. */
    private HashMap<String, String> branches;

    /** The current branch committing to in this repo.*/
    private String currentBranch;

    /** Arraylist of the file SHA-1 removals made by -rm. */
    private ArrayList<String> removals;

    public Repository() throws IOException {
        removals = new ArrayList<>();
        branches = new HashMap<>();
        commits = new HashMap<>();
        Commit firstCommit = new Commit();
        firstCommit.setFirstCommitDate();
        firstCommit.addParent("");
        firstCommit.setMessage("initial commit");
        firstCommit.actCommit();
        String shaID = Utils.sha1(Utils.serialize(firstCommit.returnHashList()));
        firstCommit.setSha_id(shaID);
        File firstCommitFile = Utils.join(Main.commits, firstCommit.getSHA_ID());
        Utils.writeContents(firstCommitFile, Utils.serialize(firstCommit));
        branches.put("master", firstCommit.getSHA_ID());
        commits.put(firstCommit.getSHA_ID(), firstCommit);
        head = firstCommit.getSHA_ID();
        currentBranch = "master";
    }

    public void add(String fileName) throws IOException {
        File workingDirFile = new File(fileName);
        File gitletFile = Utils.join(Main.stagingDir, fileName);
        if (!workingDirFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(1);
        } else if (gitletFile.exists()) {
            String stringW = Utils.readContentsAsString(workingDirFile);
            String stringG = Utils.readContentsAsString(gitletFile);
            if (!Utils.sha1(stringW).equals(Utils.sha1(stringG))) {
                Utils.restrictedDelete(gitletFile);
                Files.copy(workingDirFile.toPath(), Main.stagingDir.toPath());
            }
        } else {
            Files.copy(workingDirFile.toPath(), gitletFile.toPath());
        }
    }

    public void commit(String msg) throws IOException {
        String[] fileNames = Main.stagingDir.list();
        if (fileNames.length == 0) {
            reportError("No changes added to the commit.");
        }
        Commit newCommit = new Commit();
        newCommit.addParent(head);
        newCommit.setMessage(msg);
        newCommit.actCommit();
        byte[] stream = Utils.serialize(newCommit.returnHashList());
        compareWithParent(newCommit);
        String shaID = Utils.sha1(stream);
        newCommit.setSha_id(shaID);
        File newCommitFile = Utils.join(Main.commits, shaID);
        Utils.writeContents(newCommitFile, Utils.serialize(newCommit));
        commits.put(newCommit.getSHA_ID(), newCommit);
        head = newCommit.getSHA_ID();
        branches.put(currentBranch, head);
        newCommit.clearStage();
        removals.clear();
    }

    /** Puts unchanged staged blobs from the parent commit into the NEWCOMMIT. */
    public void compareWithParent(Commit newCommit) {
        Commit parentCommit = commits.get(newCommit.getParent().get(0));
        List<String> fileNames = Arrays.asList(Main.stagingDir.list());
        for (String file : parentCommit.getBlobs().keySet()) {
            //if file is not staged for removal and not in staging directory
            if (!removals.contains(file) && !fileNames.contains(file)) {
                //copies files that are unchanged from previous commit
                String fileID = parentCommit.getBlobs().get(file);
//                File parent = Utils.join(Main.blobs, file);
//                File newFile = Utils.join(Main.stagingDir, fileID);
//                if (sameContent(parent, newFile)) {
//                    newCommit.getBlobs().put(file, parentCommit.getBlobs().get(file));
//                }
                newCommit.getBlobs().put(file, file);
            }
        }
    }

    public void remove(String fileName) {
        //TODO  most likely a bug in here
        File stageDirFile = Utils.join(Main.stagingDir, fileName);
        Commit currentCommit = commits.get(head);
        boolean tracked = currentCommit.getBlobs().containsKey(fileName);
        boolean staged = stageDirFile.exists();
        if (!tracked && !staged) {
            reportError("No reason to remove the file.");
        }
        if (staged) {
            stageDirFile.delete();
        }
        if (tracked) {
            removals.add(fileName);
            File workingDirFile = new File(fileName);
            if (workingDirFile.exists()) {
                workingDirFile.delete();
            }
        }
    }

    public void log() {
        //TODO merge stuff
        String currentString = head;
        while (!currentString.equals("")) {
            Commit theCommit = commits.get(currentString);
            currentString = theCommit.getParent().get(0);
            logPrint(theCommit);
        }
    }

    public void logPrint(Commit theCommit) {
        System.out.println("===");
        System.out.println("commit " + theCommit.getSHA_ID());
        Formatter fmt = new Formatter();
        Date d = theCommit.getCommitDate();
        fmt.format("Date: %ta %tb %te %tT %tY %tz", d, d, d, d, d, d);
        System.out.println(fmt);
        System.out.println(theCommit.getMessage());
        System.out.println("");
    }

    public void globalLog() {
        HashSet<String> commitIDs = new HashSet<>();
        for (String branch : branches.keySet()) {
            String currentString = branches.get(branch);
            while (!currentString.equals("")) {
                Commit theCommit = commits.get(currentString);
                currentString = theCommit.getParent().get(0);
                if (!commitIDs.contains(theCommit)) {
                    currentString = theCommit.getParent().get(0);
                    logPrint(theCommit);
                }
                commitIDs.add(theCommit.getSHA_ID());
            }
        }
    }

    public void find(String msg) {
        HashSet<String> commitIDs = new HashSet<>();
        for (String branch : branches.keySet()) {
            String currentString = branches.get(branch);
            while (!currentString.equals("")) {
                Commit theCommit = commits.get(currentString);
                if (theCommit.getMessage().equals(msg)) {
                    commitIDs.add(currentString);
                }
                currentString = theCommit.getParent().get(0);
            }
        }
        if (commitIDs.isEmpty()) {
            reportError("Found no commit with that message.");
        }
        for (String commitID : commitIDs) {
            System.out.println(commitID);
        }
    }

    public void checkoutFile(String fileName) throws IOException {
        File workingDirFile = new File(fileName);
        Commit currentCommit = commits.get(head);
        //TODO
        File commitDir = Utils.join(Main.blobs, currentCommit.getBlobs().get(fileName));
        Utils.restrictedDelete(workingDirFile);
        Files.copy(commitDir.toPath(), workingDirFile.toPath());
    }

    public Commit commitExists(String commitID) {
        int length = commitID.length();
        for (String commit : commits.keySet()) {
            boolean t = commit.equals(commitID);
            if (commit.substring(0, length).equals(commitID) || t) {
                return commits.get(commit);
            }
        }
        return null;
    }

    public void checkoutFile(String commitID, String fileName) throws IOException {
        Commit theCommit = commitExists(commitID);
        if (theCommit == null) {
            reportError("No commit with that id exists.");
        }
        checkoutFileHelper(theCommit, fileName);
    }

    public void checkoutFileHelper(Commit theCommit, String fileName) throws IOException {
        File workingDirFile = new File(fileName);
        File commitDir = Utils.join(Main.blobs, theCommit.getBlobs().get(fileName));
        if (!commitDir.exists()) {
            reportError("File does not exist in that commit.");
        }
        Utils.restrictedDelete(workingDirFile);
        Files.copy(commitDir.toPath(), workingDirFile.toPath());
    }

    public void checkoutBranch(String branchName) throws IOException {
        if (branchName.equals(currentBranch)) {
            reportError("No need to checkout the current branch.");
        } else if (!branches.containsKey(branchName)) {
            reportError("No such branch exists.");
        }
        Commit checkedCommit = commits.get(branches.get(branchName));
        Commit current = commits.get(head);
        // deletes any files that are tracked by current
//        for (String fileName : current.getBlobs().keySet()) {
//            //if a file is tracked in current and also in checkBranch
////            if (checkedCommit.getBlobs().containsKey(fileName)) {
////                Utils.restrictedDelete(new File(current.getBlobs().get(fileName)));
////            }
//            File deletedFile = new File(fileName);
//            deletedFile.delete();
//        }
        // deletes any files that are in working directory and rewrite
        for (String fileName : checkedCommit.getBlobs().keySet()) {
            String fileID = checkedCommit.getBlobs().get(fileName);
            File workingDir = Utils.join(Main.CWD, fileName);
            File commitDir = Utils.join(Main.blobs, fileID);
//            workingDir.delete();
            if (workingDir.exists()) {
                workingDir.delete();
            }
            Files.copy(commitDir.toPath(), workingDir.toPath());
        }
        //filenames of working directory
        List<String> fileNames = Arrays.asList(Main.CWD.list());
        for (String fileName : fileNames) {
            if (!fileName.equals(".gitlet") && !checkedCommit.getBlobs().containsKey(fileName)) {
                File toBeDeleted = new File(fileName);
                toBeDeleted.delete();
            }
        }
        currentBranch = branchName;
        head = checkedCommit.getSHA_ID();
        checkedCommit.clearStage();
        removals.clear();
    }

    /** Status up for this repository on branches and files. */
    public void status() {
        Commit currentCommit = commits.get(head);
        //TODO need to sort
        System.out.println("=== Branches ===");
        for (String branch : branches.keySet()) {
            if (branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        String[] fileNames = Main.stagingDir.list();
        for (int i = 0; i < fileNames.length; i++) {
            System.out.println(fileNames[i]);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String file : removals) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        HashMap<String, String> modifications = new HashMap<>();
        for (String fileName : currentCommit.getBlobs().keySet()) {
            File workingDirFile = new File(fileName);
            File stageDirFile = Utils.join(Main.stagingDir, fileName);
            File blobDir = Utils.join(Main.blobs, currentCommit.getBlobs().get(fileName));
            if (stageDirFile.exists()) {
                if (!workingDirFile.exists()) {
                    modifications.put(fileName, "(deleted)");
                } else if (!sameContent(workingDirFile, stageDirFile)){
                    modifications.put(fileName, "(modified)");
                }
            } else {
                if (workingDirFile.exists()) {
                    if (!sameContent(workingDirFile, blobDir)) {
                        modifications.put(fileName, "(modified)");
                    }
                } else {
                    if (!removals.contains(fileName)) {
                        modifications.put(fileName, "(deleted)");
                    }
                }
            }
        }
        for (String fileName : modifications.keySet()) {
            System.out.println(fileName + " " + modifications.get(fileName));
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        ArrayList<String> untracked = new ArrayList<>();
        String[] currDirNames = Main.CWD.list();
        for (int i = 0; i > currDirNames.length; i++) {
            File stageDirFile = Utils.join(Main.stagingDir, fileNames);
            if (!stageDirFile.exists()) {
                if (!currentCommit.getBlobs().containsKey(currDirNames[i])) {
                    untracked.add(currDirNames[i]);
                }
            }
        }
        for (String fileName : untracked) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    public boolean sameContent(File file1, File file2) {
        String string1 = Utils.readContentsAsString(file1);
        String string2 = Utils.readContentsAsString(file2);
        if (Utils.sha1(string1).equals(Utils.sha1(string2))) {
            return true;
        }
        return false;
    }

    /** Creates a new branch from BRANCHNAME. */
    public void branch(String branchName) {
        if (branches.containsKey(branchName)) {
            reportError("A branch with that name already exists.");
        }
        branches.put(branchName, head);
    }

    /** Removes the branch in this repo with BRANCHNAME. */
    public void removeBranch(String branchName) {
        branches.remove(branchName);
    }

    public void reset(String commitID) throws IOException {
        Commit theCommit = commitExists(commitID);
        Commit currentCommit = commits.get(head);
        if (theCommit == null) {
            reportError("No commit with that id exists.");
        }
        String[] currDirNames = Main.CWD.list();
        for (int i = 0; i < currDirNames.length; i++) {
            if (!currentCommit.getBlobs().containsKey(currDirNames[i])) {
                reportError("There is an untracked file in the way; delete it or commit it first.");
            }
        }
        for (String fileName : theCommit.getBlobs().keySet()) {
            checkoutFileHelper(theCommit, fileName);
        }
        head = theCommit.getSHA_ID();
    }

    public void merge(String branchName) {

    }

    public void reportError(String msg) {
        System.out.println(msg);
        System.exit(0);
    }
}
