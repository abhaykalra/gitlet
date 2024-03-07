package gitlet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/** Main class for Gitlet.
 *  @author Abhay Kalra
 */
public class Main {

    /** ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        switch (args[0]) {
        case "init":
            init(args);
            break;
        case "add":
            add(args);
            break;
        case "commit":
            commit(args);
            break;
        case "checkout":
            checkout(args);
            break;
        case "log":
            log(args);
            break;
        case "global-log":
            globalLog(args);
            break;
        case "branch":
            branch(args);
            break;
        case "rm":
            remove(args);
            break;
        case "rm-branch":
            removeBranch(args);
            break;
        case "find":
            find(args);
            break;
        case "status":
            status(args);
            break;
        case "reset":
            reset(args);
            break;
        case "merge":
            merge(args); break;
        case "print":
            if (new File(".", args[1]).exists()) {
                System.out.println(Utils
                        .readContentsAsString(new File(".", args[1])));
            } else {
                System.out.println("File does not exist in working directory");
            }
            break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }




    /** Checks if exist.
     * @return boolean */
    public static boolean ifexists() {
        return GITLET.exists() && COMMITS.exists()
                && HEAD.exists() && STAGE.exists()
                && OBJECTS.exists() && CURRCOMMIT.exists()
                && BRANCHES.exists();
    }


    /** Initializes  folder.
     * @param args a */
    public static void init(String[] args) {

        if (args.length != 1) {
            throw new RuntimeException(
                    String.format("Invalid number of "
                            + "arguments for: init"));
        }

        if (!GITLET.exists()) {
            GITLET.mkdir();
        } else {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);

        }

        BRANCHES.mkdir();
        COMMITS.mkdir();
        STAGE.mkdir();
        OBJECTS.mkdir();

        Commit firstCommit = new Commit(true);
        File commitFile = Utils.join(COMMITS,
                firstCommit.getID() + ".txt");
        Utils.writeObject(commitFile, firstCommit);

        File master = Utils.join(BRANCHES, "master.txt");
        Utils.writeContents(master, firstCommit.getID());

        Utils.writeContents(HEAD, "branches/master.txt");

        Utils.writeObject(CURRCOMMIT, new Commit());
    }


    /** add  folder.
     * @param args a */
    public static void add(String[] args) throws IOException {

        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        if (args.length != 2) {
            throw new RuntimeException(
                    String.format("Invalid number of "
                            + "arguments for: add"));
        }

        File add = Utils.join(".", args[1]);
        if (!add.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Commit currCommit = Utils.readObject(CURRCOMMIT,
                Commit.class);
        String contents = Utils.readContentsAsString(add);
        File stage = Utils.join(STAGE, Utils.sha1(contents));
        Commit headCommit = commithelper();
        if (currCommit.getFileToID().containsKey(
                add.getCanonicalPath())) {
            File removedFile = getFile(currCommit.getIDFromFile(
                    add.getCanonicalPath()), STAGE.listFiles());
            removedFile.delete();
            currCommit.getFileToID().remove(add.getCanonicalPath());

        } else if (!headCommit.getIDFromFile(add.getCanonicalPath())
                .equals(Utils.sha1(contents))) {
            Utils.writeContents(stage, contents);
            currCommit.addFile(add);
        }
        Utils.writeObject(CURRCOMMIT, currCommit);
    }
    /** commit  folder.
     * @param args a */
    public static void commit(String[] args) {

        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        Commit commit = Utils.readObject(CURRCOMMIT, Commit.class);

        String second = "";
        if (args.length == 3 && args[0].equals("commit-merge")) {
            second = args[2];
        } else if (args.length != 2 || args[1].isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        commit.setMessage(args[1]);


        File[] files = STAGE.listFiles();

        if (files.length == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        for (File file: files) {
            File object = Utils.join(OBJECTS, file.getName());
            Utils.writeContents(object, Utils.readContents(file));

            file.delete();
        }

        commit.createTimestamp();

        Commit pastCommit = commithelper();

        if (second.isEmpty()) {
            commit.setParent(pastCommit.getID());
        } else {
            commit.setParent(pastCommit.getID() + " " + second);
        }
        Utils.writeContents(headhelper(), commit.getID());

        File newCommit = Utils.join(COMMITS, commit.getID() + ".txt");
        Utils.writeObject(newCommit, commit);

        Utils.writeObject(CURRCOMMIT, new Commit());

    }




    /** Initializes  folder.
     * @return File a */
    public static File headhelper() {
        String path = Utils.readContentsAsString(HEAD);
        return Utils.join(GITLET, path);
    }

    /** Initializes  folder.
     * @return File a */
    public static Commit commithelper() {
        File branch = headhelper();
        String commitID = Utils.readContentsAsString(branch);
        File commitFile = Utils.join(COMMITS, commitID + ".txt");

        if (getFile(commitID + ".txt", COMMITS.listFiles()) == null) {
            System.out.println("the commit does not exist");
            System.exit(-1);
        }

        return Utils.readObject(commitFile, Commit.class);
    }


    /** Initializes  folder.
     * @param args a*/
    public static void checkout(String[] args) throws IOException {
        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (args.length == 3 && args[1].equals("--")) {
            Commit head = commithelper();
            File file = Utils.join(new File("."), args[2]);
            revert(file, head);
        } else if (args.length == 4 && args[2].equals("--")) {
            File commitFile;
            if (args[1].length() < Utils.UID_LENGTH) {
                commitFile = getFileShort(args[1],
                        COMMITS.listFiles());
            } else {
                commitFile = getFile(args[1] + ".txt",
                        COMMITS.listFiles());
            }
            if (commitFile == null) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            Commit commit = Utils.readObject(commitFile, Commit.class);
            File file = Utils.join(new File("."), args[3]);
            revert(file, commit);
        } else if (args.length == 2) {
            File branch = getFile(args[1] + ".txt",
                    BRANCHES.listFiles());
            if (branch == null) {
                System.out.println("No such branch exists.");
                System.exit(0);
            } else if (headhelper().getName().equals(branch.getName())) {
                System.out.println("No need to checkout the current branch.");
                System.exit(0);
            }
            String commitID = Utils.readContentsAsString(branch);
            File commitFile = Utils.join(COMMITS, commitID + ".txt");
            Commit commit = Utils.readObject(commitFile, Commit.class);
            Commit headCommit = commithelper();
            untrackedFileError(commit, headCommit);
            for (String filePath: commit.getFileToID().keySet()) {
                revert(Utils.join(new File(filePath)), commit);
            }
            for (String filePath: headCommit.getFileToID().keySet()) {
                if (!commit.getFileToID().containsKey(filePath)) {
                    (new File(filePath)).delete();
                }
            }
            for (File file: STAGE.listFiles()) {
                file.delete();
            }
            Utils.writeObject(CURRCOMMIT, new Commit());
            Utils.writeContents(HEAD,
                    "branches/" + args[1] + ".txt");
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Reverts.
     * @param f name of  file
     * @param c commit  of the file.*/
    public static void revert(File f, Commit c) throws IOException {
        if (!c.getFileToID().containsKey(f.getCanonicalPath())) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String contentsID = c.getIDFromFile(f.getCanonicalPath());
        if (contentsID.substring(0, 7).equals("remove*")) {
            f.delete();
        } else {
            File file = getFile(contentsID, OBJECTS.listFiles());
            if (file == null) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
            String contents = Utils.readContentsAsString(file);
            Utils.writeContents(f, contents);
        }
    }

    /** Reverts.
     * @param name a
     * @param files f
     * @return File a.*/
    public static File getFile(String name, File[] files) {
        if (name.isEmpty()) {
            return null;
        }
        for (File file: files) {
            if (file.getName().equals(name)) {
                return file;
            }
        }
        return null;
    }

    /** Reverts.
     * @param name a
     * @param files f
     * @return File a.*/
    public static File getFileShort(String name, File[] files) {
        if (name.isEmpty()) {
            return null;
        }
        for (File file: files) {
            if (file.getName().substring(0, name.length()).equals(name)) {
                return file;
            }
        }
        return null;
    }


    /** Reverts.
     * @param commit a
     * @param headCommit f. */
    public static void untrackedFileError(Commit commit, Commit headCommit) {
        for (String filePath: commit.getFileToID().keySet()) {
            if (new File(filePath).exists()) {
                String currentContents = Utils.sha1(
                        Utils.readContentsAsString(new File(filePath)));
                if (!headCommit.getFileToID().containsKey(filePath)
                        && !currentContents.equals(commit.getIDFromFile(
                        filePath))
                        && !(commit.getIDFromFile(filePath)).substring(0, 7).
                        equals("remove*")) {
                    System.out.println("There is an untracked "
                            + "file in the way delete it, "
                            + "or add and commit it first.");
                    System.exit(0);
                }
            }
        }
    }

    /** Reverts.
     * @param args a. */
    public static void branch(String[] args) {

        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        if (args.length != 2) {
            throw new RuntimeException(
                    String.format("Invalid number of "
                            + "arguments for: branch"));
        }

        File newBranch = Utils.join(BRANCHES, args[1] + ".txt");
        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Utils.writeContents(newBranch, commithelper().getID());
    }


    /** Reverts.
     * @param args a. */
    public static void log(String[] args) {
        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        if (args.length != 1) {
            throw new RuntimeException(
                    String.format("Invalid number of "
                            + "arguments for: log"));
        }

        Commit head = commithelper();
        while (head != null) {
            printCommit(head);
            String parent = head.getParent();
            if (parent.length() > Utils.UID_LENGTH) {
                parent = parent.substring(0, Utils.UID_LENGTH);
            }
            File parentFile = getFile(parent + ".txt",
                    COMMITS.listFiles());
            if (parentFile != null) {
                head = Utils.readObject(parentFile, Commit.class);
            } else {
                head = null;
            }
        }

    }
    /** Reverts.
     * @param commit a. */
    public static void printCommit(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getID());
        System.out.println("Date: " + commit.getTimestamp());
        System.out.println(commit.getMessage() + "\n");
    }





    /** Reverts.
     * @param args a. */
    public static void globalLog(String[] args) {

        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        if (args.length != 1) {
            throw new RuntimeException(
                    String.format("Invalid number of "
                            + "arguments for: add"));
        }

        for (File commitFile: COMMITS.listFiles()) {
            Commit commit = Utils.readObject(commitFile, Commit.class);
            printCommit(commit);
        }
    }

    /** Reverts.
     * @param args a. */
    public static void find(String[] args) {

        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        if (args.length != 2) {
            throw new RuntimeException(
                    String.format("Invalid number of "
                            + "arguments for: add"));
        }

        ArrayList<String> equalcommit = new ArrayList<String>();
        for (File commitFile: COMMITS.listFiles()) {
            Commit commit = Utils.readObject(commitFile, Commit.class);
            if (commit.getMessage().equals(args[1])) {
                equalcommit.add(commit.getID());
            }
        }

        if (equalcommit.size() == 0) {
            System.out.println("Found no commit with that message");
            System.exit(0);
        } else {
            for (String commitID: equalcommit) {
                System.out.println(commitID);
            }
        }
    }

    /** Reverts.
     * @param args a. */
    public static void removeBranch(String[] args) {

        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        if (args.length != 2) {
            throw new RuntimeException(
                    String.format("Invalid number of "
                            + "arguments for: rm-branch"));
        }
        File branchFile = getFile(args[1] + ".txt",
                BRANCHES.listFiles());
        if (branchFile == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (headhelper().getName().equals(branchFile.getName())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else {
            branchFile.delete();
        }
    }


    /** Reverts.
     * @param args a. */
    public static void remove(String[] args) throws IOException {
        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (args.length != 2) {
            throw new RuntimeException(
                    String.format("Invalid number of "
                            + "arguments for: add"));
        }
        boolean notpresent = true;
        Commit currentCommit = Utils.readObject(CURRCOMMIT,
                Commit.class);
        File removeFile = Utils.join(".", args[1]);
        String removeFilePathway = removeFile.getCanonicalPath();

        if (currentCommit.getFileToID().containsKey(removeFilePathway)) {
            Utils.join(STAGE, currentCommit.getIDFromFile(
                    removeFilePathway)).delete();
            currentCommit.getFileToID().remove(removeFilePathway);
            notpresent = false;
        }

        if (commithelper().getFileToID().containsKey(removeFilePathway)) {
            currentCommit.addRemoveFile(removeFile);
            File stageFile = Utils.join(STAGE,
                    currentCommit.getIDFromFile(removeFilePathway));
            if (removeFile.exists()) {
                Utils.writeContents(stageFile,
                        Utils.readContentsAsString(removeFile));
                removeFile.delete();
            } else {
                Utils.writeContents(stageFile, "");
            }
            notpresent = false;
        }
        Utils.writeObject(CURRCOMMIT, currentCommit);
        if (notpresent) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

    }


    /** Reverts.
     * @param args a. */
    public static void status(String[] args) throws IOException {
        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (args.length != 1) {
            throw new RuntimeException(String.format("Invalid number of "
                            + "arguments for: add"));
        }
        System.out.println("=== Branches ===");
        String headBranch = headhelper().getName();
        System.out.println("*" + headBranch.
                substring(0, headBranch.length() - 4));
        File[] files = BRANCHES.listFiles();
        Arrays.sort(files);
        for (File file: files) {
            if (!file.getName().equals(headBranch)) {
                System.out.println(file.getName()
                        .substring(0, file.getName().length() - 4));
            }
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        files = STAGE.listFiles();
        ArrayList<File> removed = new ArrayList<File>();
        Arrays.sort(files);
        for (File file: files) {
            if (!file.getName().substring(0, 7).equals("remove*")) {
                Commit commit = Utils.readObject(CURRCOMMIT, Commit.class);
                File foundFile = new File(commit.getFileFromID(file.getName()));
                System.out.println(foundFile.getName());
            } else {
                removed.add(file);
            }
        }
        System.out.println("");
        System.out.println("=== Removed Files ===");
        for (File file: removed) {
            Commit commit = Utils.readObject(CURRCOMMIT, Commit.class);
            File foundFile = new File(commit.getFileFromID(file.getName()));
            System.out.println(foundFile.getName());
        }
        System.out.println("");
        System.out.println("=== Modifications Not Staged For Commit ===");
        HashMap<File, String> modifiedNotStaged = notstaged();
        ArrayList<File> sortedFiles =
                new ArrayList<File>(modifiedNotStaged.keySet());
        Collections.sort(sortedFiles);
        for (File file: sortedFiles) {
            System.out.println(file.getName() + " ("
                    + modifiedNotStaged.get(file) + ")");
        }
        System.out.println("");
        System.out.println("=== Untracked Files ===");
        ArrayList<File> untracked = untracked();
        Collections.sort(untracked);
        for (File file: untracked) {
            System.out.println(file.getName());
        }
    }






    /** Reverts.
     * @return a.*/
    public static HashMap<File, String> notstaged() {
        HashMap<File, String> result = new HashMap<File, String>();
        Commit currentCommit = Utils.readObject(CURRCOMMIT,
                Commit.class);
        Commit headCommit = commithelper();
        for (String filePath: headCommit.getFileToID().keySet()) {
            File f = new File(filePath);
            if (f.exists()) {
                if (!headCommit.getIDFromFile(filePath).equals(
                        Utils.sha1(Utils.readContentsAsString(f)))
                        && !currentCommit.getFileToID().containsKey(filePath)) {
                    result.put(f, "modified");
                }
            } else if (currentCommit.getFileToID().containsKey(filePath)) {
                if (!currentCommit.getIDFromFile(filePath).
                        substring(0, 7).equals("remove*")) {
                    result.put(f, "deleted");
                }
            } else if (!headCommit.getIDFromFile(filePath).
                    substring(0, 7).equals("remove*")) {
                result.put(f, "deleted");
            }
        }

        for (String filePath: currentCommit.getFileToID().keySet()) {
            File f = new File(filePath);
            if (!currentCommit.getIDFromFile(filePath).
                    substring(0, 7).equals("remove*") && f.exists()) {
                if (!currentCommit.getIDFromFile(filePath).
                        equals(Utils.sha1(Utils.readContentsAsString(f)))) {
                    result.put(f, "modified");
                }
            } else if (!currentCommit.getIDFromFile(filePath).
                    substring(0, 7).equals("remove*") && !f.exists()) {
                result.put(f, "deleted");
            }
        }

        return result;
    }

    /** Reverts.
     * @return a.*/
    public static ArrayList<File> untracked() throws IOException {
        ArrayList<File> result = new ArrayList<File>();
        File currentDirectory = new File(".");
        Commit currentCommit = Utils.readObject(CURRCOMMIT,
                Commit.class);
        Commit headCommit = commithelper();
        for (File f: currentDirectory.listFiles()) {
            if (!headCommit.getFileToID().containsKey(
                    f.getCanonicalPath())
                    && !f.getName().equals(".gitlet")) {
                if (!currentCommit.getFileToID().containsKey(
                        f.getCanonicalPath())) {

                    result.add(f);
                } else if (currentCommit.getIDFromFile(
                                f.getCanonicalPath()).substring(0, 7).
                        equals("remove*")
                        && f.exists()) {
                    result.add(f);
                }
            }
        }
        result.remove(GITLET);
        return result;
    }

    /** Reverts.
     * @param args a.*/
    public static void reset(String[] args) throws IOException {

        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        if (getFile(args[1]  + ".txt",
                COMMITS.listFiles()) == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else if (commithelper().getID().equals(args[1])) {
            System.out.println("No need to reset to the current commit.");
            System.exit(0);
        }

        if (args.length != 2) {
            throw new RuntimeException(
                    String.format("Invalid number of "
                            + "arguments for: add"));
        }

        String prevpath = Utils.readContentsAsString(HEAD);
        String branch = Utils.sha1("branchToBeUsedForCheckout");
        String[] input = {"branch", branch};
        branch(input);
        Utils.writeContents(Utils.join(BRANCHES,
                branch + ".txt"), args[1]);
        input[0] = "checkout";
        checkout(input);
        Utils.writeContents(HEAD, prevpath);
        Utils.writeContents(headhelper(), args[1]);
        input[0] = "rm-branch";
        removeBranch(input);
    }

    /** Reverts.
     * @param args a.*/
    public static void merge(String[] args) throws IOException {
        if (!ifexists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        File branchFile = getFile(args[1] + ".txt", BRANCHES.listFiles());
        if (branchFile == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (headhelper().getName().equals(branchFile.getName())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (STAGE.listFiles().length != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        String commitID = Utils.readContentsAsString(branchFile);
        File commitFile = getFile(commitID + ".txt", COMMITS.listFiles());
        Commit commit = Utils.readObject(commitFile, Commit.class);
        Commit headCommit = commithelper();
        untrackedFileError(commit, headCommit);
        Commit splitCommit = findpoint(commit, headCommit);
        if (splitCommit == null) {
            System.out.println("Unknown error, split commit not found");
            System.exit(0);
        } else if (splitCommit.getID().equals(commitID)) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            System.exit(0);
        } else if (splitCommit.getID().equals(headCommit.getID())) {
            String[] input = {"checkout", args[1]};
            checkout(input);
            System.out.println("Current branch fast-forwarded.");
        }
        mergehelper(commit, headCommit, splitCommit);
        for (String filePath: headCommit.getFileToID().keySet()) {
            if (!commit.getFileToID().containsKey(filePath)
                    && !splitCommit.getFileToID().containsKey(filePath)) {
                stageFile(new File(filePath), Utils.readContentsAsString
                        (new File(filePath)), false);
            }
        }
        findsplit(commit, headCommit, splitCommit);
        boolean conflict = conflicthelper(commit, headCommit);
        String[] input = {"commit-merge", "Merged " + args[1] + " into "
                + headhelper().getName().substring(0, headhelper()
                        .getName().length() - 4) + ".", commitID};
        commit(input);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }
    /** Reverts.
     * @param commit a.
     * @param headCommit b
     * @param splitCommit c.*/
    public static void mergehelper(Commit commit, Commit
            headCommit, Commit splitCommit) throws IOException {
        for (String filePath: commit.getFileToID().keySet()) {
            File f = new File(filePath);
            boolean cond1 = splitCommit.getFileToID().containsKey(filePath)
                    && !commit.getIDFromFile(filePath).
                    equals(splitCommit.getIDFromFile(filePath))
                    && !checkmodified(filePath,
                    splitCommit.getIDFromFile(filePath),
                    headCommit, splitCommit);
            boolean cond2 = !splitCommit.getFileToID().containsKey(filePath)
                    && !headCommit.getFileToID().containsKey(filePath)
                    && !f.exists();
            if (cond1 || cond2) {
                revert(f, commit);
                if (commit.getIDFromFile(filePath).substring(0, 7).
                        equals("remove*")) {
                    stageFile(f, "", true);
                } else {
                    stageFile(f, Utils.readContentsAsString(f), false);
                }
            }
        }
    }
    /** Reverts.
     * @param f a.
     * @param contents a
     * @param remove a*/
    public static void stageFile(File f, String contents,
                                 boolean remove) throws IOException {
        File stage = Utils.join(STAGE, Utils.sha1(contents));
        if (remove) {
            stage = Utils.join(STAGE,
                    "remove*" + f.getName());
        }
        Utils.writeContents(stage, contents);
        Commit currentCommit = Utils.readObject(CURRCOMMIT,
                Commit.class);
        if (remove) {
            currentCommit.addRemoveFile(f);
        } else {
            currentCommit.addFile(f);
        }
        Utils.writeObject(CURRCOMMIT, currentCommit);
    }
    /** Reverts.
     * @param branch1Commit a.
     * @param branch2Commit a
     * @return a.*/
    public static Commit findpoint(Commit branch1Commit,
                                        Commit branch2Commit) {
        Commit commit1 = branch1Commit;
        Commit commit2 = branch2Commit;
        while (commit1 != null && commit2 != null) {
            if (commit1.getID().equals(commit2.getID())
                    || commit2.getParent().equals(commit1.getID())) {
                return commit1;
            } else if (commit1.getParent().equals(commit2.getID())) {
                return commit2;
            }

            if (commit1.getParent().isEmpty()) {
                commit1 = null;
            } else {
                commit1 = Utils.readObject(getFile(
                        commit1.getParent() + ".txt",
                        COMMITS.listFiles()), Commit.class);
            }

            if (commit2.getParent().isEmpty()) {
                commit2 = null;
            } else {
                commit2 = Utils.readObject(getFile(
                        commit2.getParent() + ".txt",
                        COMMITS.listFiles()), Commit.class);
            }
        }

        return null;

    }

    /** Reverts.
     * @param filePath a.
     * @param currentContentsID a
     * @param start a
     * @param end a.
     * @return a. */
    public static boolean checkmodified(String filePath,
                                            String currentContentsID,
                                            Commit start, Commit end) {
        Commit current = start;
        while (!current.getID().equals(end.getID())) {
            if (current.getFileToID().containsKey(filePath)
                    && !current.getIDFromFile(filePath)
                    .equals(currentContentsID)) {
                return true;
            }
            current = Utils.readObject(getFile(current.getParent()
                    + ".txt", COMMITS.listFiles()), Commit.class);
        }
        return false;
    }
    /** Reverts.
     * @param commit a.
     * @param headCommit a
     * @param splitCommit a*/
    public static void findsplit(Commit commit,
                                        Commit headCommit,
                                        Commit splitCommit) throws IOException {
        for (String filePath : splitCommit.getFileToID().keySet()) {
            File f = new File(filePath);
            if (!commit.getFileToID().containsKey(filePath)
                    && !checkmodified(filePath, splitCommit.getIDFromFile(
                    filePath), headCommit, splitCommit)) {
                f.delete();
                Commit curr = Utils.readObject(CURRCOMMIT,
                        Commit.class);
                if (curr.getFileToID().containsKey(filePath)) {
                    Utils.join(STAGE, curr.getIDFromFile(
                            filePath)).delete();
                    curr.getFileToID().remove(filePath);
                }
            } else if (!headCommit.getFileToID().containsKey(filePath)
                    && checkmodified(filePath, splitCommit.getIDFromFile(
                    filePath), commit, splitCommit)) {
                stageFile(f, "", true);
                f.delete();
            }
        }
    }
    /** Reverts.
     * @param commit a.
     * @param headCommit a
     * @return a. */
    public static boolean conflicthelper(
            Commit commit,
            Commit headCommit) throws IOException {
        boolean conflict = false;
        for (String filePath: commit.getFileToID().keySet()) {
            if (headCommit.getFileToID().containsKey(filePath)
                    && !commit.getIDFromFile(filePath).equals(
                    headCommit.getIDFromFile(filePath))) {
                File headFileContents = getFile(headCommit.
                        getIDFromFile(filePath), OBJECTS.listFiles());
                File fileContents = getFile(commit.getIDFromFile(filePath),
                        OBJECTS.listFiles());
                String headContents = Utils.readContentsAsString(
                        headFileContents);
                String contents = Utils.readContentsAsString(fileContents);
                if (headFileContents.getName().substring(0, 7).
                        equals("remove*")) {
                    headContents = "";
                } else if (fileContents.getName().substring(0, 7).
                        equals("remove*")) {
                    contents = "";
                }
                String combined = "<<<<<<< HEAD\n" + headContents
                        + "=======\n" + contents + ">>>>>>>\n";
                Utils.writeContents(new File(filePath), combined);
                stageFile(new File(filePath), combined, false);
                conflict = true;
            }
        }
        return conflict;
    }
    /** doc. */
    static final File GITLET = new File(".gitlet");
    /** doc. */
    static final File HEAD = new File(".gitlet/head.txt");
    /** doc. */
    static final File BRANCHES = new File(".gitlet/branches");
    /** doc. */
    static final File COMMITS = new File(".gitlet/commits");
    /** doc. */
    static final File STAGE = new File(".gitlet/stage");
    /** doc. */
    static final File OBJECTS = new File(".gitlet/objects");
    /** doc. */
    static final File CURRCOMMIT = new File(".gitlet/commit.txt");
}
