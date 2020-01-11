# SuggestionBot

Bot for Minecraft Mod suggestions

## Commands
### New Pool
Create a new Suggestion Pool by using: 
`%newpool name channelID channelID2`
  
  > name = name of the pool
  >
  > channelID = Channel where the bot will post suggestions to get voted on
  >
  > channelID2 = Channel where suggestions will be posted by users for discussion
### Add Suggestion
Add new suggestions by using: 
`%sugg link_to_mod`

  (only curseforge links are accepted)
  
### Edit Mode
Enter editmode by using: 
`%editmode`

Adds the user as an editor and adds reactions to all current suggestions (if not already present) to approve or reject them
(you need to be an editor to approve or deny suggestions)

### Stop editing
Stop editmode by using: 
`%finishedit`

Will stop editmode and remove all suggestion messages that are pending (have been approved or rejected)

Will also output a csv file with the results of those suggestions


 
