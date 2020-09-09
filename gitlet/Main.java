package gitlet;


import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Felix Yu
 */
public class Main {

    /** The repository of this directory. */
    private static Repository repo;

    static final File gitletDir = new File(".gitlet");
    static final File stagingDir = Utils.join(gitletDir, "staging");
    static final File blobs = Utils.join(gitletDir, "blobs");
    static final File commits = Utils.join(gitletDir, "commits");
    static final File repoFile = Utils.join(gitletDir, "repo");
    static final File CWD = new File(".");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        // FILL THIS IN
        if (args.length != 0) {
            if (gitletDir.exists()) {
                repo = Utils.readObject(repoFile, Repository.class);
            }
            switch(args[0]) {
                case "init":
                    checkOperands(1, args);
                    initRepo();
                    break;
                case "add":
                    checkError(2, args);
                    add(args[1]);
                    break;
                case "commit":
                    checkError(2, args);
                    commit(args[1]);
                    break;
                case "rm":
                    checkError(2, args);
                    remove(args[1]);
                    break;
                case "log":
                    checkError(1, args);
                    log();
                    break;
                case "global-log":
                    checkError(1, args);
                    globalLog();
                    break;
                case "find":
                    checkError(2, args);
                    find(args[1]);
                    break;
                case "checkout":
                    checkout(args);
                    break;
                case "status":
                    status();
                    break;
                case "branch":
                    checkError(2, args);
                    branch(args[1]);
                    break;
                case "rm-branch":
                    checkError(2, args);
                    removeBranch(args[1]);
                    break;
                case "reset":
                    checkError(2, args);
                    reset(args[1]);
                    break;
                case "merge":
                    checkError(2, args);
                    merge(args[1]);
                default:
                    System.out.println("No command with that name exists.");
                    System.exit(0);
            }
            if (!gitletDir.exists()) {
                repoFile.createNewFile();
            }
            Utils.writeObject(repoFile, repo);
        } else {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
    }

    /** Called when user inputs incorrect number of operands. */
    public static void checkOperands(int correctNum, String[] args) {
        if (args.length != correctNum) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void checkError(int correctNum, String[] args) {
        repoExists();
        checkOperands(correctNum, args);
    }

    public static void repoExists() {
        if (!gitletDir.exists()) {
            System.out.println("Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
    }

    /** Initializes a gitlet repo in this directory,
     * if the directory is already a repo, exit,
     * else create master and a new commit. */
    public static void initRepo() throws IOException {
        File tempFile = new File(".gitlet");
        if (tempFile.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        gitletDir.mkdir();
        stagingDir.mkdir();
        blobs.mkdir();
        commits.mkdir();
        repo = new Repository();
    }

    public static void add(String fileName) throws IOException {
        repo.add(fileName);
    }

    public static void commit(String msg) throws IOException {
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        repo.commit(msg);
    }

    public static void remove(String fileName) {
        repo.remove(fileName);
    }

    public static void removeBranch(String branchName) {
        repo.removeBranch(branchName);
    }

    public static void log() {
        repo.log();
    }

    public static void globalLog() {
        repo.globalLog();
    }

    public static void find(String msg) {
        repo.find(msg);
    }

    public static void status() {
        repo.status();
    }

    /** Handles ARGS from main and calls the corresponding method based on ARGS. */
    public static void checkout(String... args) throws IOException {
        if (args.length > 4 || args.length < 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        if (args.length == 4) {
            if (args[2].equals("--")) {
                checkoutFile(args[1], args[3]);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else if (args.length == 3) {
            if (args[1].equals("--")) {
                checkoutFile(args[2]);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else {
            checkoutBranch(args[1]);
        }
    }

    /** Takes the version of the file as it exists in the head commit,
     *  the front of the current branch, and puts it in the working directory,
     *  overwriting the version of the file that's already there if there is one.
     *  The new version of the file is not staged.*/
    public static void checkoutFile(String fileName) throws IOException {
        repo.checkoutFile(fileName);
    }
    /** Takes the version of the file as it exists in the
     * commit with the given id, and puts it in the working directory,
     * overwriting the version of the file that's already there if there is one.
     * The new version of the file is not staged.*/
    public static void checkoutFile(String commitID, String fileName) throws IOException {
        repo.checkoutFile(commitID, fileName);
    }

    public static void checkoutBranch(String branchName) throws IOException {
        repo.checkoutBranch(branchName);
    }

    public static void branch(String branch) {
        repo.branch(branch);
    }

    /** Checks out all the files tracked by the given COMMITID.
     * Removes tracked files that are not present in that commit.*/
    public static void reset(String commitID) throws IOException {
        repo.reset(commitID);
    }

    public static void merge(String branchName) {
        repo.merge(branchName);
    }

}
