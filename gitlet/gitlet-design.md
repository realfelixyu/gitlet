# Gitlet Design Document

# Classes and Data Structures


![](https://paper-attachments.dropbox.com/s_7EED569A9EA94B3BE98FF4E9FAD34E8737A07B58F1F0DBA041DE6F1D5A2DF8A1_1586838560809_image.png)

# Main.java:

This class is the entry point of the program and parses the program inputs and has functions corresponding to each command.(UML not most up to date)

## Fields:
1. Repository repo: A repository that contains commits and functions corresponding to commands


![](https://paper-attachments.dropbox.com/s_7EED569A9EA94B3BE98FF4E9FAD34E8737A07B58F1F0DBA041DE6F1D5A2DF8A1_1586838600078_image.png)

# Commit.java:

Represents a commit and contains information of a commit

## Fields:
1. String sha_id: Unique ID of a commit from SHA-1 hash
2. String[] parent: array of SHA-1 hash of this commit’s parents
3. Date date: Date when this commit was made
4. HashMap blobs: Map of blobs’ filenames and their hash 
5. String message: The message of the commit


![](https://paper-attachments.dropbox.com/s_7EED569A9EA94B3BE98FF4E9FAD34E8737A07B58F1F0DBA041DE6F1D5A2DF8A1_1586838619950_image.png)

# Repository.java:

Represents a repository and keeps track of all the commits while also contains the main algorithms such as adding and checking out.
Fields:

1. HashMap commits: A map from commits’ IDs to the commit objects
2. HashMap branches: A map from branch name to its current commit SHA-1 hash
3. String head: The SHA-1 hash id of the current commit
# Algorithms


## init

Add a .gitlet directory to current working directory if there isn’t already a .gitlet file in current working directory. If there’s already a .gitlet directory then exit with a message. Initialize master branch with message `initial commit` and no files.

## add

If file doesn’t exist print `File does not exist.` Make a copy of the file to the staging directory only if the current commit’s file is not the same as the file to be added (e.g hashcodes are different).

## commit 

Add parent’s filenames and SHA-1 hashcodes to HashMap. Go through files in the staging area and move file to objects/commits directory and rename the file to it’s SHA-1 hash and add their filename and SHA-1 hashcodes to HashMap. Remove filenames from HashMap if they are in remove directory. Clear staging area and remove directory.

## remove

Check if the file is in staging directory or in head’s HashMap’s keys. Check if the file name is in the staging directory, if yes then remove from staging directory and add file name to remove.

## log

Start at head and iterate through children. Print commit. (Note: special case with merged commits, {there’s two parents}) 

## globalLog

Go through all files in commits and print.

## find

Parse the input message. Iterate through commits, if message contains parsed string print out commit id.(Note: separate lines for multiple IDs)

## status

Prints out branches, current staged files, removed files. Check if there are modifications not staged by checking current commit and staged files. Also prints out names of files in working directory that is not staged.

## checkout

Replace file in current working directory with file in specified commit, if not specified use last commit.
If commit specified but filename not, clear all files from working directory, and replace them with files specified commit. Change HEAD to specified commit.

## branch

If the branch name doesn’t exist already, adds a new entry in branches HashMap. Entry would be like branches.put(branch name, head). Else print error message.

## rmBranch

If branch is in HashMap, remove it.

## reset

Calls checkout [commit id], clear staging area.

## merge

Looks at the common ancestor(split point) of the two branches. Look at the commit HashMaps of the two branches and the split point. The other branch has precedence over the current branch in terms of modifications. 


# Persistence
## Commits

Commit objects will be serialized and written to .gitlet/commits/[SHA-id]. 

## Blobs

Blobs will be written to .gitlet/blobs/[SHA-id].

## Staging

Files staged for addition will be in .gitlet/staging/[filename].

## Removal

Files staged for removal will have a empty file with the same filename as the file to be removed in .gitlet/removal.

## Repository

The repository object will be serialized and loaded, the commit HashMap will not be serialized. The repository will be stored in .gitlet/repo.

