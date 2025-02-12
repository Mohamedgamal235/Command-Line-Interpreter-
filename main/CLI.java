package org.AssignemntOS;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.*;
import java.io.File;
import java.util.*;
import java.util.Scanner;

public class CLI {

    private Path currDir = Paths.get(System.getProperty("user.dir"));

    public void run() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Ready to execute your commands. Let's get started");
        while (true){
            System.out.print("> ");
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting CLI.");
                return;
            }

            String[] parts = input.split(" ");
            String command = parts[0];
            String argument = parts.length > 1 ? parts[1] : "";

            if (input.contains("|")){
                String[] cmds = new String[2];
                cmds[0] = input.substring(0,input.indexOf("|")).trim() ;
                cmds[1] = input.substring(input.indexOf("|")+1).trim();

                String[] cmdsPart1 = cmds[0].split(" ");
                String[] cmdsPart2 = cmds[1].split(" ");

                executePipe(cmdsPart1 , cmdsPart2);
            }
            else if (input.contains(">") && !input.contains(">>")){
                String[] str = input.split(" ");
                String content  = execute(str) ;
                redirect(content , str[str.length - 1], false);
            }
            else if (input.contains(">>")){
                String[] str = input.split(" ");
                String content  = execute(str) ;
                redirect(content , str[str.length - 1] , true);
            }
            else if (command.equalsIgnoreCase("pwd")){
                System.out.println(pwd());
            }
            else if (command.equalsIgnoreCase("cd")){
                cd(argument);
            }
            else if (command.equalsIgnoreCase("help")){
                help();
            }
            else if (command.equalsIgnoreCase("ls")){
                if (parts.length > 1 && argument.equals("-R"))
                    lsRecursive(currDir.toFile(), "");
                else if (parts.length > 1 && "-r".equals(parts[1])) {
                    System.out.println(String.join("\n", lsReverse()));}
                else if (parts.length == 1){
                    ArrayList<String> files = ls();
                    System.out.println(String.join("\n" , files));
                }
                else if (parts.length > 1 && argument.equals("-a")){
                    ArrayList<String> files = ls();
                    System.out.println(String.join("\n" , files));
                }
                else {
                    System.out.println("Error: Command not recognized.");
                }
            }
            else if (command.equalsIgnoreCase("mkdir")){
                if (parts.length>1) {
                    String dirNames = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
                    mkdir(dirNames);
                }
                else
                    System.out.println("Error: No directory name specified.");
            }
            else if (command.equalsIgnoreCase("rmdir")){
                if (parts.length>1) {
                    String dirNames = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
                    rmdir(dirNames);
                }
                else
                    System.out.println("Error: No directory name specified.");
            }
            else if (command.equalsIgnoreCase("touch")){
                if (parts.length>1) {
                    String fileNames = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
                    touch(fileNames);
                }
                else
                    System.out.println("Error: No file name specified.");
            }
            else if (command.equalsIgnoreCase("mv")){
                StringBuilder s = new StringBuilder();
                for (int i = 1 ; i < parts.length ; i++)
                    s.append(parts[i]).append(" ");
                mv(s.toString());
            }
            else if (command.equalsIgnoreCase("rm")){
                String[] str = new String[parts.length - 1];
                for (int i = 1 ; i < parts.length ; i++)
                    str[i-1] = parts[i] ;
                rm(str);
            }
            else if (command.equalsIgnoreCase("cat")){
                String[] str = new String[parts.length - 1];
                for (int i = 1 ; i < parts.length ; i++)
                    str[i-1] = parts[i] ;
                System.out.println(cat(str)) ;
            }
        }
    }

    // -------------------------- Commands --------------------------------------
    // --------------------------------------------------------------------------

    public String pwd(){
        return currDir.toString() ;
    }

    // -----------------------------------------------
    // -----------------------------------------------

    public void cd(String path){
        path = path.trim();
        if (path.equals("..")){
            currDir = currDir.getParent();
            if (currDir == null)
                currDir = Paths.get(System.getProperty("user.dir"));
        }
        else {
            Path newDir = currDir.resolve(path);
            if (Files.exists(newDir) && Files.isDirectory(newDir))
                currDir = newDir ;
            else
                System.out.println("Invalid Path");
        }
    }

    // -----------------------------------------------
    // -----------------------------------------------

    public ArrayList<String> ls(){
        ArrayList<String> files = new ArrayList<>();
        if (Files.isDirectory(currDir)){
            try{
                DirectoryStream<Path> streamDir = Files.newDirectoryStream(currDir);
                for (Path file : streamDir)
                    files.add(file.getFileName().toString());

                Collections.sort(files);
            }
            catch (IOException e){
                System.out.println(e.getMessage());
            }
        }
        else
            System.out.println("Current path not directory");

        return files;
    }

    // -----------------------------------------------
    // -----------------------------------------------

    public void mv(String str){
        List<String> files = new ArrayList<>(Arrays.asList(str.split(" ")));

        Path target = currDir.resolve(files.get(files.size() - 1)); // last file or dir
        List<Path> source = new ArrayList<>(); // list if multiple sorces
        for (int i = 0 ; i < files.size() - 1 ; i++)
            source.add(currDir.resolve(files.get(i)));

        try{
            if (source.size() == 1){

                Path src = source.get(0);

                if (!Files.exists(src)) {
                    throw new FileNotFoundException(src.getFileName() + " -> Not Found");
                }

                if (Files.isDirectory(src)){
                    if (Files.notExists(target)){
                        Files.move(src , src.resolveSibling(target.toString()));
                    }
                    else if (Files.isDirectory(target)){
                        Files.move(src, target.resolve(src.getFileName()) , StandardCopyOption.REPLACE_EXISTING);
                    }
                    else {
                        throw new FileNotFoundException("Cannot overwrite non-directory " + src.toFile().getName() + " with directory " + target.toFile().getName());
                    }
                }
                else if (!Files.isDirectory(src)){
                    if (Files.notExists(target)){
                        Files.move(src , src.resolveSibling(target.toString())); // rename
                    }
                    else if (Files.isDirectory(target)){
                        Files.move(src, target.resolve(src.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    }
                    else {
                        Files.move(src, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            else { //  multiple sources
                if (Files.notExists(target) || !Files.isDirectory(target)) {
                    throw new IOException("Target must be an existing directory for multiple sources");
                }

                for (Path file : source){
                    if (Files.notExists(file)) {
                        throw new FileNotFoundException("Source file/directory does not exist: " + file.toFile().getName());
                    }
                    else
                        Files.move(file, target.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    // -----------------------------------------------
    // -----------------------------------------------

    public void rm(String[] parts) {
        int i = 0;
        boolean recursive = false;

        if (parts.length > 0 && (parts[0].equals("-f") || parts[0].equals("-r"))) {
            if (parts[0].equals("-r"))
                recursive = true;
            i = 1;
        }

        for (; i < parts.length; i++) {
            Path rmTarget = new File(parts[i]).toPath();

            if (recursive && rmTarget.toFile().isDirectory()) {
                try {
                    deleteDirectory(rmTarget);
                } catch (IOException e) {
                    System.out.println("An I/O error occurred while deleting the directory.");
                }
            } else {
                if (rmTarget.toFile().isDirectory()) {
                    System.out.println("Can't remove '" + parts[i] + "', it is a directory.");
                } else if (rmTarget.toFile().exists() ) {
                    try {
                        Files.deleteIfExists(rmTarget);
                    } catch (IOException e) {
                        System.out.println("An I/O error occurred while deleting the file.");
                    }
                } else {
                    System.out.println("File '" + parts[i] + "' does not exist.");
                }
            }
        }
    }

    private static void deleteDirectory(Path directory) throws IOException {
        if (Files.isDirectory(directory)) {
            try (var entries = Files.newDirectoryStream(directory)) {
                for (Path entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        Files.delete(directory);
    }


    // -----------------------------------------------
    // -----------------------------------------------

    public String cat(String[] files) {
        StringBuilder allContent = new StringBuilder(); // to append

        for (int i = 0; i < files.length; i++) {
            Path catTarget = new File(files[i]).toPath();

            if (catTarget.toFile().isDirectory()) {
                System.out.println("can't cat '" + files[i] + "', it is a directory.");
                continue;
            }
            else if (catTarget.toFile().exists()) {
                File file = new File(files[i]);
                try (Scanner fileScanner = new Scanner(file)) {
                    while (fileScanner.hasNextLine()) {
                        allContent.append(fileScanner.nextLine());
                        if (fileScanner.hasNextLine()) {
                            allContent.append("\n");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("An I/O error occurred while reading '" + files[i] + "'.");
                }

                if (i < files.length - 1) {
                    allContent.append("\n");
                }
            } else {
                System.out.println("File '" + files[i] + "' does not exist.");
            }
        }

        return allContent.toString();
    }


    // -----------------------------------------------
    // -----------------------------------------------

    public void executePipe(String[] cmd1 , String[] cmd2){
         // ls | cat
        // echo | cat

        if (cmd1[0].equals("ls") && cmd2[0].equals("cat")){
            ArrayList<String> items = ls();
            String res = String.join("\n" , items);
            if (cmd2.length == 1){  // ls | cat
                System.out.println(res);
            }
            else if (cmd2.length == 3 && cmd2[1].equals(">")){ // ls | cat > file1.txt
                redirect(res, cmd2[2], false);
            }
            else if (cmd2.length == 3 && cmd2[1].equals(">>")){
                redirect(res, cmd2[2], true);
            }
            else if (cmd2.length == 4 && cmd2[2].equals(">")){ // ls | cat file1.txt > file2.txt
                String content = execute(cmd2);
                redirect(content, cmd2[3], false);
            }
            else if (cmd2.length == 4 && cmd2[2].equals(">>")){
                String content = execute(cmd2);
                redirect(content, cmd2[3], true);
            }
        }
        else if (cmd1[0].equals("echo") && cmd2[0].equals("cat")){ // Echo
            String str = execute(cmd1);

            if (cmd2.length == 1){  // echo "string" | cat
                System.out.println(str);
            }
            else if (cmd2.length == 3 && cmd2[1].equals(">")){ // echo "stirng" | cat > file1.txt
                redirect(str, cmd2[2] , false);
            }
            else if (cmd2.length == 3 && cmd2[1].equals(">>")){
                redirect(str, cmd2[2], true);
            }
            else if (cmd2.length == 4 && cmd2[2].equals(">")){ // echo "stirng" | cat file1.txt > file2.txt
                String content = execute(cmd2);
                redirect(content, cmd2[3], false);
            }
            else if (cmd2.length == 4 && cmd2[2].equals(">>")){
                String content = execute(cmd2);
                redirect(content, cmd2[3], true);
            }
        }
        else
            System.out.println("with pipe use only (ls , cat , echo , > , >>).");
    }

    // -----------------------------------------------
    // -----------------------------------------------

    private String execute(String[] str){

        String command = str[0].trim();
        String content  = "" ;

        if (command.equals("ls")) {
            ArrayList<String> files = ls();
            content = String.join("\n" , files);
        }
        else if (command.equals("echo")){
            StringBuilder s = new StringBuilder();
            for (int i = 1 ; i < str.length ; i++){
                if (str[i].equals(">"))
                    break;
                s.append(str[i]).append(" ");
            }
            content = s.toString();
        }
        else if (command.equals("cat"))
            content = cat(str[1].split(" "));

        return content;
    }

    public void redirect(String content , String file , boolean append){
        try{
            FileWriter w = new FileWriter(file , append);
            w.write(content);
            w.write("\n");
            w.close();
        }
        catch (IOException e){
            System.out.println("Error writing ti file : " + e.getMessage());
        }
    }

    // -----------------------------------------------
    // -----------------------------------------------

    public void mkdir(String dirNames){
        String[] dirs = dirNames.split(" ");
        for (String dirname : dirs){
            dirname = dirname.trim();
            if (!dirname.isEmpty()){
                Path newDir = currDir.resolve(dirname);
                try {
                    if (Files.exists(newDir)){
                        System.out.println("Error: Directory already exists: " + dirname);
                    } else{
                        Files.createDirectories(newDir);
                        System.out.println("Directory created: " + dirname);
                    }
                } catch (IOException e){
                    System.out.println("Error: Could not create directory: " + dirname + ". " + e.getMessage());
                }
            }
        }
    }

    // -----------------------------------------------
    // -----------------------------------------------

    public void rmdir(String dirNames) {
        String[] dirs = dirNames.split(" ");
        for (String dirname : dirs) {
            dirname = dirname.trim();
            if (!dirname.isEmpty()){
                Path dir = currDir.resolve(dirname);
                try {
                    if (Files.exists(dir) && Files.isDirectory(dir)) {
                        if (Files.list(dir).findAny().isPresent()) {
                            System.out.println("Error: Directory not empty: " + dirname);
                        } else {
                            Files.delete(dir);
                            System.out.println("Directory removed: " + dirname);
                        }
                    } else {
                        System.out.println("Error: Directory does not exist: " + dirname);
                    }
                } catch (IOException e) {
                    System.out.println("Error: Could not delete directory: " + dirname + ". " + e.getMessage());
                }
            }
        }
    }

    // -----------------------------------------------
    // -----------------------------------------------

    public void touch(String fileNames){
        String[] files=fileNames.split(" ");
        for(String fileName : files){
            fileName = fileName.trim();
            if (!fileName.isEmpty()){
                File file = new File(currDir.toString(), fileName);
                try {
                    if (file.exists()) {
                        boolean success = file.setLastModified(System.currentTimeMillis());
                        if (success) {
                            System.out.println("Timestamp updated successfully for: " +fileName);
                        } else {
                            System.out.println("Error: Could not update Timestamp for: " +fileName);
                        }
                    } else {
                        if (file.createNewFile()) {
                            System.out.println("File created: " + fileName );
                        } else {
                            System.out.println("Error: File could not be created: " +fileName);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("I/O Error: There was an issue creating or updating the file: " +fileName);
                }
            }
        }
    }

    // -----------------------------------------------
    // -----------------------------------------------

    public void lsRecursive(File dir, String indent){
        String[] files = dir.list();
        if (files != null){
            for (String file : files){
                File currentFile = new File(dir, file);
                System.out.println(indent + file);
                if(currentFile.isDirectory()){
                    lsRecursive(currentFile, indent + "    ");
                }
            }
        } else {
            System.out.println("Error: Unable to list files.");
        }
    }

    // -----------------------------------------------
    // -----------------------------------------------

    public List<String> lsReverse() {
        List<String> files = new ArrayList<>();
        if (Files.isDirectory(currDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(currDir)) {
                for (Path file : stream) {
                    files.add(file.getFileName().toString());
                }
                files.sort(Collections.reverseOrder());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Current path not a directory");
        }
        return files;
    }

    // -----------------------------------------------
    // -----------------------------------------------

    public void help(){
        System.out.println("cd          : Displays the name of or changes the current directory.");
        System.out.println("pwd         : Prints the working directory");
        System.out.println("ls          : Lists the contents (files & directories) of the current directory sorted alphabetically.");
        System.out.println("ls -r       : Print Lists the contents (files & directories) on reverse order." );
        System.out.println("ls -a       : display all contents even entries starting with (a).");
        System.out.println("mkdir       : Creates a directory with each given name.");
        System.out.println("rmdir       : Removes each given directory (only if it is empty).");
        System.out.println("touch       : Creates a file with each given name.");
        System.out.println("mv          : Moves one or more files/directories to a directory.");
        System.out.println("rm          : Removes one or more files/directories from the current directory.");
        System.out.println("cat         : Concatenates the content of files and prints it.");
        System.out.println(">           : Redirects the output of the first command to be written to a file.");
        System.out.println(">>          : Like > but appends to file if it exists.");
        System.out.println("|           : Pipes | redirect the output of the previous command as in input to another command.");
    }

}