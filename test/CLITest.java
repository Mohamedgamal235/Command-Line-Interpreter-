import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static java.nio.file.Files.readString;
import static org.junit.jupiter.api.Assertions.*;
import org.AssignemntOS.CLI;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Arrays;
public class CLITest {

    private CLI cli;
    private Path testDir;


@BeforeEach
    void setUp() throws IOException {
        cli = new CLI();
        testDir = Paths.get(System.getProperty("java.io.tmpdir"), "cli_test");
        Files.createDirectories(testDir);
        cli.cd(testDir.toString());
    }



    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(testDir)) {
            Files.walk(testDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
//---------------------------------------------------------
    //---------------------------------------------------------

    @Test
    void pwdTest() {
        String currDir = cli.pwd();
        assertEquals(testDir.toString(), currDir , "pwd should return the current directory.");
    }    
//---------------------------------------------------------
    //---------------------------------------------------------

    @Test
    void testCdValidDirectory() { // cd Ayaa
        cli.mkdir("Ayaa");
        cli.cd("Ayaa");
        assertEquals(testDir.resolve("Ayaa").toString(), cli.pwd(), "Should change to testDir.");
    }

    @Test
    void testCdParentDirectory(){ // cd ..
        cli.mkdir("parent");
        cli.cd("parent");
        cli.mkdir("child");
        cli.cd("child");
        cli.cd("..");
        assertEquals(testDir.resolve("parent").toString(), cli.pwd(), "Should change to testDir.");
    }

    //---------------------------------------------------------
    //---------------------------------------------------------
    //---------------------------------------------------------
    //---------------------------------------------------------

    @Test
    void testLs(){
        cli.touch("File1.txt");
        cli.touch("File2.txt");
        cli.mkdir("Gemmy");
        cli.touch("File3.txt");

        String expected = "File1.txt\nFile2.txt\nFile3.txt\nGemmy";
        String currLs = String.join("\n", cli.ls());
        assertEquals(expected.trim() , currLs, "ls should print all file and directories.");
    }

    //---------------------------------------------------------
    //---------------------------------------------------------

    @Test
    void testMv() {
        cli.touch("File1.txt");
        cli.touch("File2.txt");
        cli.touch("File3.txt");
        cli.mkdir("dirc");

        // move
        cli.mv("File1.txt dirc/File1.txt");
        assertTrue(Files.exists(testDir.resolve("dirc/File1.txt")), "File.txt should move to dirc");
        assertFalse(Files.exists(testDir.resolve("file1.txt")), "file1.txt should not move to dirc");

        // rename
        cli.mv("File3.txt mohamed.txt");
        assertTrue(Files.exists(testDir.resolve("mohamed.txt")), "file3.txt should be renamed to mohamed.txt.");


        // Move multiple files to the dir
        cli.touch("test1.txt");
        cli.touch("test2.txt");
        cli.touch("test3.txt");

        cli.mv("test1.txt test2.txt test3.txt dirc");

        assertTrue(Files.exists(testDir.resolve("dirc/test1.txt")), "test1.txt should move to dirc");
        assertTrue(Files.exists(testDir.resolve("dirc/test2.txt")), "test2.txt should move to dirc");
        assertTrue(Files.exists(testDir.resolve("dirc/test3.txt")), "test3.txt should move to dirc");
    }

    //---------------------------------------------------------
    //---------------------------------------------------------

    @Test
    void redirectTest() throws IOException {
        String fileName = "file3.txt";
        Path filePath = testDir.resolve(fileName);

        cli.touch(fileName);

        String expectedContent = "Welcome to Command Line Interpreter";
        cli.redirect(expectedContent, filePath.toString(), false);

        assertTrue(Files.exists(filePath), "The file should exist after redirecting content.");

        String writtenContent = Files.readString(filePath).stripTrailing();

        assertEquals(expectedContent, writtenContent, "The content should match after redirecting.");
    }


    @Test
    void appendTest() throws IOException {
        cli.touch("file3.txt");
        Path filePath = testDir.resolve("file3.txt");

        assertTrue(Files.exists(filePath), "The file should exist after creating.");

        String initialContent = "Welcome to Command Line Interpreter";
        cli.redirect(initialContent, filePath.toString(), false); // write

        String appendedContent = "This is an appended line.";
        cli.redirect(appendedContent, filePath.toString(), true); // Append content

        String forTestWritten = Files.readString(filePath).stripTrailing();

        assertEquals(initialContent + "\n" + appendedContent , forTestWritten, "The content should match after appending.");
    }

    //---------------------------------------------------------
    //---------------------------------------------------------

    @Test
    void testRmFile() throws IOException {
        Path fileToRemove = testDir.resolve("rmfile.txt");
        Files.createFile(fileToRemove);
        assertTrue(Files.exists(fileToRemove), "File should exist before deletion.");
        cli.rm(new String[]{"rmfile.txt"});
        assertTrue(Files.exists(fileToRemove), "File should be deleted.");
    }

    @Test
    void testRmDirectory() throws IOException {
        Path dirToRemove = testDir.resolve("rmdir");
        Files.createDirectories(dirToRemove);
        assertTrue(Files.exists(dirToRemove), "Directory should exist before deletion.");
        cli.rm(new String[]{"-r", "rmdir"});
        assertTrue(Files.exists(dirToRemove), "Directory should be deleted.");
    }

    @Test
    void testRmNonExistFile() {
        String fileName = "fileNotExist.txt";
        cli.rm(new String[]{fileName});
    }

    @Test
    void testRmFileUsingForce() throws IOException {
        Path fileToForceDelete = testDir.resolve("file.txt");
        Files.createFile(fileToForceDelete);
        assertTrue(Files.exists(fileToForceDelete), "File should exist before deletion.");
        cli.rm(new String[]{"-f", "file.txt"});
        assertTrue(Files.exists(fileToForceDelete), "File should be deleted.");
    }

    //---------------------------------------------------------
    //---------------------------------------------------------

    @Test
    void testMkdir() {
        cli.mkdir("dir1");
        assertTrue(Files.exists(testDir.resolve("dir1")), "dir1 should be created.");
        cli.mkdir("dir1");
        cli.mkdir("dir2 dir3");
        assertTrue(Files.exists(testDir.resolve("dir2")), "dir2 should be created.");
        assertTrue(Files.exists(testDir.resolve("dir3")), "dir3 should be created.");
    }

    //---------------------------------------------------------
    //---------------------------------------------------------

    @Test
    void testRmdir() throws IOException {
        cli.mkdir("RemoveDir");
        assertTrue(Files.exists(testDir.resolve("RemoveDir")), "RemoveDir should exist.");

        cli.rmdir("RemoveDir");
        assertFalse(Files.exists(testDir.resolve("RemoveDir")), "Directory should be removed.");
        cli.mkdir("DirNotEmpty");
        cli.touch("DirNotEmpty/file.txt");
        cli.rmdir("DirNotEmpty");
    }

    //---------------------------------------------------------
    //---------------------------------------------------------

    @Test
    void testTouch() {
        cli.touch("file1.txt");
        assertTrue(Files.exists(testDir.resolve("file1.txt")), "File should be created.");
        cli.touch("file1.txt");
        assertTrue(Files.exists(testDir.resolve("file1.txt")), "File should still exist after timestamp update.");
    }

    //---------------------------------------------------------
    //---------------------------------------------------------

    @Test
    void testLsReverse() throws IOException {
        cli.touch("file3.txt");
        cli.touch("file4.txt");
        cli.touch("file5.txt");
        List<String> expectedFiles = Arrays.asList("file5.txt", "file4.txt", "file3.txt");
        List<String> outputFiles = cli.lsReverse();
        assertEquals(expectedFiles, outputFiles, "Files should be listed in reverse alphabetical order.");
    }

    //---------------------------------------------------------
    //---------------------------------------------------------

    @Test
    void testLsRecursive() throws IOException {
        cli.mkdir("RecursiveDir");
        cli.mkdir("RecursiveDir2");
        cli.touch("RecursiveDir2/file1.txt");
        cli.touch("RecursiveDir/file2.txt");
        cli.lsRecursive(testDir.toFile(), "");
    }


    //---------------------------------------------------------
    //---------------------------------------------------------


}
