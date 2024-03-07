package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.io.File;
import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Abhay Kalra
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    /** Tests that init correctly initializes the directories and files,
     * should be run after the first init is called.*/
    @Test
    public void testInit() {

    }

    /** Tests that .gitlet correctly adds the first file, to be run after
     * init and add have been run once respectively. */
    @Test
    public void testAdd() {

    }

    /** Tests that .gitlet correctly adds the first file, to be run after
     * init and add have been run once respectively. */
    @Test
    public void testCommit() {

    }

    /** The .gitlet folder storing all of the data. */
    static final File GITLET = new File(".gitlet");

    /** The head file pointing to the head branch */
    static final File HEAD = new File(".gitlet/head.txt");

    /** The branches folder containing each of the branches */
    static final File BRANCHES = new File(".gitlet/branches");

    /** The commits folder containing all of the commits */
    static final File COMMITS = new File(".gitlet/commits");

    /** The stage folder containing all added files to be committed */
    static final File STAGE = new File(".gitlet/stage");

    /** The objects folder containing all committed blobs (files) */
    static final File OBJECTS = new File(".gitlet/objects");

    /** The current commit file containing the serialized current commit */
    static final File CURRCOMMIT = new File(".gitlet/commit.txt");

}

