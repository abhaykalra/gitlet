# Gitlet Design Document

**Name**: Abhay Kalra

## Classes and Data Structures

###Directory
stores a directory (std file structure)

####Fields
1. pw: A string representing the path of the file to the home directory
2. fs: An ArrayList representing a linked list of the files and directories in this directory
3. is_diff: A HashMap holding all of the changed files that need to be committed
4. cs: An ArrayList of all commits that have been done
5. ts: A string representing the timestamp of the latest commit
   (could consider making a Timestamp object)
6. n: The string name of the directory

###Commit
This class stores all information around a commit

####Fields
1.  files, comment, t, id 

###Main
This class runs Gitlet through directories, files, and commits

####Fields
1. run: A scanner for the Gitlet
2. conn: A HashMap connecting  files and directory names to their respective directory/file pathway
3. rep: the main directory for  other files, master directory

## Algorithms

###Directory
1. dir(String name): The class constructor. Create a new directory with a pathway and initiate all instance variables to empty, and establishes the name instance variable and pathway instance variable.
2. save(String name): Saves the status of the file or directory as it is, and creates a new file with the given name if one doesn't already exist.
3. remove(String name): removes the file or directory
4. commit(String comment): commits anything that has been added, and does this through creating a new instance of commit
5. log(): prints out every commit done from the instance variable _commits
6. add(String name): adds a file/s or directory to be committed

###Commit
1. Commit(String comment, ArrayList files): updates the files and directories given, creates a new commit with an ID and timestamp
2. find(String ID): searches all the commits through the HashMap commits and returns the ones with the applicable ID

###Main
creates a new master directory and allows for the user to type in commands, master directory is rep
.....................
## Persistence
By starting up the program, it will create a repo directory and an initial commit through the command init(). Based on the command run, structure of files will be saved.
add, init, commit, merge

Lyrics
We're no strangers to love
You know the rules and so do I
A full commitment's what I'm thinking of
You wouldn't get this from any other guy
I just wanna tell you how I'm feeling
Gotta make you understand
Never gonna give you up
Never gonna let you down
Never gonna run around and desert you
Never gonna make you cry
Never gonna say goodbye
Never gonna tell a lie and hurt you
We've known each other for so long
Your heart's been aching but you're too shy to say it
Inside we both know what's been going on
We know the game and we're gonna play it
And if you ask me how I'm feeling
Don't tell me you're too blind to see
Never gonna give you up
Never gonna let you down
Never gonna run around and desert you
Never gonna make you cry
Never gonna say goodbye
Never gonna tell a lie and hurt you
Never gonna give you up
Never gonna let you down
Never gonna run around and desert you
Never gonna make you cry
Never gonna say goodbye
Never gonna tell a lie and hurt you
Never gonna give, never gonna give
(Give you up)
We've known each other for so long
Your heart's been aching but you're too shy to say it
Inside we both know what's been going on
We know the game and we're gonna play it
I just wanna tell you how I'm feeling
Gotta make you understand
Never gonna give you up
Never gonna let you down
Never gonna run around and desert you
Never gonna make you cry
Never gonna say goodbye
Never gonna tell a lie and hurt you
Never gonna give you up
Never gonna let you down
Never gonna run around and desert you
Never gonna make you cry
Never gonna say goodbye
Never gonna tell a lie and hurt you
Never gonna give you up
Never gonna let you down
Never gonna run around and desert you
Never gonna make you cry
Never gonna say goodbye
