# ESD Soundboard App
Soundboard App for the Elgato Stream Deck (ESD from now on).

## Managing sounds

In the Sound menu you can manage all sounds added to the App. Removal of sounds is not implemented yet:

![Sounds](https://raw.githubusercontent.com/WElRD/Images/master/ESDSoundboardApp/Frontend-04-Manage-Sounds.png)

To add sounds, press the "+" button beneath the Field with the sounds.

![Add a sound](https://raw.githubusercontent.com/WElRD/Images/master/ESDSoundboardApp/Frontend-05-Add-Sounds.png)

Here you can add a new sound to your soundboard library. After clicking on "Ok" the sound will be copied into a subfolder of this app. When choosing a mp3 file that contains a cover, the cover will be set as an icon. The icon can be changed by pressing "Open" beneath it.

You can choose if its an ambience sound or an effect sound. Tags a separated by a space character. The name sound only contain the characters a-z, A-Z, _, -. Th icon besides the name indicates if the name is already taken or not.

## Soundboards

List of Soundboards and the SoftStreamDeck:

![Frontend](https://raw.githubusercontent.com/WElRD/Images/master/ESDSoundboardApp/Frontend-01.png)

Here you can edit you soundboards by clicking on existing eintries or add a new one by clicking on "+".

![New Soundboard](https://raw.githubusercontent.com/WElRD/Images/master/ESDSoundboardApp/Frontend-02-New-Soundboard.png)

Here you can name/rename your soundboard and add/remove themes contained by it. Adding a theme can be done by clicking on the "+" above the Theme Field:

![Add Theme](https://raw.githubusercontent.com/WElRD/Images/master/ESDSoundboardApp/Frontend-03-New-Theme.png)

Here you can add Ambience and Effect sounds. Both types will be displayed seperatly on the ESD and ambience sounds will be repeated, while effects will be played once. Effects cannot be stopped, ambience sounds on the other hand cann be stopped and started. 

Both dropdown menus for ambience and effect sounds can be filtered by typing in them. The sounds will be filtered by name and by tags.