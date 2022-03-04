#!/bin/bash
############################################################
# Constants                                                #
############################################################
REMOTE="origin"
TEAM="team" # Replace with real team

CREATE="Create"
UPDATE="Update"

############################################################
# Variables                                                #
############################################################
MainBranch="main"
RunLintCheck=false
PRMode=$UPDATE

############################################################
# Help                                                     #
############################################################
Help()
{
   # Display Help
   echo "This script will automate most of the PR create/update process for an Android project"
   echo "Including"
   echo "- running lint check"
   echo "- squashing commits against a branch. When creating a PR, commits will be squashed against"
   echo "the main branch. And when updating it, they'll be squashed against the remote branch"
   echo "- commit changes. Will ask for commit message"
   echo "- push changes to remote"
   echo "- create PR on GitHub. GitHub CLI should be installed and authenticated"
   echo
   echo "To use"
   echo "- put the script in the root of your Android project"
   echo "- edit the constants if necessary"
   echo "- install GitHub cli"
   echo "- run gh auth login"
   echo "- select github.com, select ssh, skip selecting ssh key, login with browser"
   echo "- [Do If Getting Authentication Error] create personal access token to login"
   echo "- [Do If Still Getting Authentication Error] create ssh key for GitHub cli, run gh auth login and select the created key this time"
   echo
   echo "Syntax: ./pr.sh [-l|c|u|b|h]"
   echo "options:"
   echo "l     Skip lint checks"
   echo "c     Will create a new PR with this branch. Squashing will be done against the main branch"
   echo "u     Will update the existing PR. This is the default. Squashing is done against current branch's remote branch"
   echo "b     Set the main branch to squash commits against. Takes an argument. You can also update the MainBranch var in the script. Default is main"
   echo "h     Show help"
   echo
   echo "Examples"
   echo "./pr.sh -c"
   echo "This will run the lint check, squash commits against main branch, ask for commit message, push the changes to remote (will create new remote branch if doesn't exist) and create a PR"
   echo
   echo "./pr.sh"
   echo "This will run the lint check, squash commits against main branch, ask for commit message and push the changes to remote (will create new remote branch if doesn't exist)"
   echo "Run if updating the current PR after making changes"
   echo
   echo "./pr.sh -cb feature-self-reports"
   echo "This will run the lint check, squash commits against feature-self-reports branch, ask for commit message, push the changes to remote (will create new remote branch if doesn't exist) and create a PR against feature-self-reports branch"
   echo
}

############################################################
# LintCheck                                                #
############################################################
LintCheck()
{
   echo "LintCheck Start"
   if ! ./gradlew ktlintFormat;
   then
     exit $?
   fi
   if ! ./gradlew ktlintCheck;
   then
     exit $?
   fi
   echo "LintCheck End"
}

############################################################
# SquashCommits                                            #
############################################################
SquashCommits()
{
   current=$(git branch --show-current)
   if [ "$#" -ne 1 ]
   then
     against="$REMOTE/$current"
   else
     against=$MainBranch
   fi

   echo "SquashCommits Start against $against"
   echo "Squashing all commits from $current"
   if ! git reset "$(git merge-base $against "$current")";
   then
     exit $?
   fi
   echo "SquashCommits End"
}

############################################################
# Commit                                                   #
############################################################
Commit()
{
   echo "Commit Start"
   git add -A
   if ! git commit;
   then
     exit $?
   fi
   echo "Commit End"
}

############################################################
# Push                                                     #
############################################################
Push()
{
   echo "Push Start"
   branch=$(git branch --show-current)
   if ! git push -u $REMOTE "$branch";
   then
     exit $?
   fi
   echo "Push End"
}

############################################################
# Create PR                                                #
############################################################
CreatePR()
{
   echo "CreatePR Start"
   # if ! gh pr create --fill --reviewer $TEAM --base $MainBranch;
   if ! gh pr create --fill --base $MainBranch;
   then
     exit $?
   fi
   echo "CreatePR End"
}

############################################################
# Check if Changelog added                                 #
############################################################
CheckChangelog()
{
   echo "CheckChangelog Start"
   unstagedFiles=$(git diff --name-only | grep -E $CHANGELOG | wc -l)
   stagedFiles=$(git diff --name-only --cached | grep -E $CHANGELOG | wc -l)

   count=$((unstagedFiles + stagedFiles))

   if [ $count -eq 0 ]
   then
     read -r -p "Changelog not added. Continue? (y/n)" continue
   else
     continue=y
   fi

   if [[ $continue != "y" && $continue != "Y" ]]
   then
     exit 1
   fi
   echo "CheckChangelog End"
}

############################################################
# Run                                                     #
############################################################
Run()
{
   #CheckChangelog

   if [ $RunLintCheck = true ]
   then
     LintCheck
   fi

   if [ $PRMode == $CREATE ]
   then
     SquashCommits $MainBranch
   else
     SquashCommits
   fi

   Commit

   Push

   if [ $PRMode == $CREATE ]
   then
     CreatePR
   fi
}

############################################################
############################################################
# Main program                                             #
############################################################
############################################################
while getopts "lcuhb:" option; do
   case $option in
      h) # display Help
         Help
         exit;;
      c) # Create PR
         PRMode=$CREATE;;
      u) # Update PR
         PRMode=$UPDATE;;
      l) # Lint check
         RunLintCheck=false;;
      b) # Set main branch commits
         MainBranch=$OPTARG;;
      \?) # Invalid option
         echo "Error: Invalid option"
         exit;;
   esac
done

Run

############################################################
# SetMode  (Not used)                                      #
############################################################
SetMode()
{
   mode=$1
   if [ "$mode" == "C" ]
   then
      PRMode=$CREATE
   elif [ "$mode" == "U" ]
   then
      PRMode=$UPDATE
   else
      echo "Invalid PR mode. Should be C (for create) or U (for update)"
      exit
   fi
}