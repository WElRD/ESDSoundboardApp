# TabletopSoundboard
This is a simple soundboard app which is designed to be used primary with the Elgato Stream Deck (ESD from now on). Allthough it also can be used without the ESD through the software implementation.
It was created with the intention to make audio usage for RPGs easier by not using up as much space like a laptop.

## Installation
Go to the [Release Page](https://github.com/VVEIRD/TabletopSoundboard/releases) to grab the latest version. If you have Java 10 or later installed, you can download the "*-no-jdk.zip". If you don't have a JRE or JDK 10 installed take the other zip file.

## Features
* Add audio files with custom covers, filterable tags; categorized into ambience and effect audio. (Ambience is looped, effect audio will only be played once).
* Combine different audio files into multiple soundboards with multiple sub-categories.
* Use your spotify playlists to expand your sound repertory.
* Integrate the Nanoleaf Light Panels to change their scene with the press of a button, while also changing the music.
* Use the software Soundboard if you dont own an ESD. Show the software Soundboard by clicking on "Show SoftDecks" 

## Managing sounds

In the Sound menu you can manage all sounds added to the App. Removal of sounds is not implemented yet:

![Sounds](https://raw.githubusercontent.com/VVEIRD/Images/master/TabletopSoundboard/Frontend-04-Manage-Sounds.png)

To add sounds, press the "+" button beneath the Field with the sounds.

![Add a sound](https://raw.githubusercontent.com/VVEIRD/Images/master/TabletopSoundboard/Frontend-05-Add-Sounds.png)

Here you can add a new sound to your soundboard library. After clicking on "Ok" the sound will be copied into a subfolder of this app. When choosing a mp3 file that contains a cover, the cover will be set as an icon. The icon can be changed by pressing "Open" beneath it.

You can choose if its an ambience sound or an effect sound. Tags are separated by a space character. The name sound can only contain the characters a-z, A-Z, _, -. Th icon besides the name indicates if the name is already taken or not.

## Soundboards

List of Soundboards and the SoftStreamDeck:

![Frontend](https://raw.githubusercontent.com/VVEIRD/Images/master/TabletopSoundboard/Frontend-01.png)

Here you can edit you soundboards by clicking on existing eintries or add a new one by clicking on "+".

![New Soundboard](https://raw.githubusercontent.com/VVEIRD/Images/master/TabletopSoundboard/Frontend-02-New-Soundboard.png)

Here you can name/rename your soundboard and add/remove themes contained by it. Adding a theme can be done by clicking on the "+" above the Theme Field:

![Add Theme](https://raw.githubusercontent.com/VVEIRD/Images/master/TabletopSoundboard/Frontend-03-New-Theme.png)

Here you can add Ambience and Effect sounds. Both types will be displayed seperatly on the ESD and ambience sounds will be repeated, while effects will be played once. Effects cannot be stopped, ambience sounds on the other hand can be stopped and started. 

If you have spotify support enabled, your spotify playlist will show up in the drop down fields and can be filtered by the additional tag "Spotify".

Both dropdown menus for ambience and effect sounds can be filtered by typing in them. The sounds will be filtered by name and by tags.

## Enabling Spotify support

To enable Spotify go to the Options Tab and enter your Client-Id and Client Secret from your Spotify-App.

![Enable Spotify](https://raw.githubusercontent.com/VVEIRD/Images/master/TabletopSoundboard/Frontend-06-Spotify-Options.png)

To create a spotify app, goto "https://developer.spotify.com/dashboard" and create an App. Name your app and select "Desktop App" From the dashboard goto the App and copy the Client ID and Secret. Goto "Edit Settings" and add the URL http://localhost:5000/spotify-redirect from "Redirect URL" to the whitelisted Redirect URIs. Scroll down to save. Then click on Login. 

For details on how to create the spotify app visit [Create a spotify App](https://github.com/VVEIRD/TabletopSoundboard/blob/master/help/Create%20a%20Spotify%20App.md)

## Enabling Nanoleaf Lights support
For details on how enable the Nanoleaf Light Panel Plugin visit [Enable Nanoleaf Plugin](https://github.com/VVEIRD/TabletopSoundboard/blob/master/help/EnableNanoleafLightsPlugin.md)
