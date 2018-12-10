;The Semi-Intelligent Language Learner - SILL - Version 2.0
;
;Starting by setting up some important parameters for the program
Parameter$=CommandLine$()
Global SILL_main		;Name of the main window.
Global SILL_lang
Global Lang
Global DesktopX			;Width of users desktop.
Global DesktopY			;Height of users desktop.
Global PositionX		;Position of main window.
Global PositionY
Global MainSizeX		;Size of main window.
Global MainSizeY
Global Language$		;Language file to currently use
Global UserInput		;User input field.
Global Conversation		;Display of the conversation so far.
Global SelectedTopic$	;Holding the topic of the conversation.
Global Response$		;SILLs last response.
Global FileName$		;The file name for the user input.
Dim MenuTxt$(12)			;Array to store the text for the main menu.
StatusUpdate=CreateTimer(1)
AppTitle "SILL v.2.0"
SeedRnd MilliSecs()

;Create the folders needed unless they already exist
If FileType(".\lng")=0 Then CreateDir(".\lng")
If FileType(".\Brain")=0 Then CreateDir(".\Brain")
If FileType(".\Brain\History")=0 Then CreateDir(".\Brain\History")
If Left$(Parameter$,1)=Chr$(32) Then Parameter$=Right$(Parameter$,Len(Parameter$)-1)
If Parameter$="/createswe" Then Notify "Creating Swedish language file...":CreateSwedish()

;Create the files Words.txt and todays history file
CreateNewFile("Brain\Words.txt")
CreateNewFile("Brain\History\"+CurrentDate$()+".txt")

;Checking to see if there's a SILL.ini file to load any information from
LoadIni=CreateNewFile("SILL.ini")
DesktopX=GadgetWidth(Desktop())
DesktopY=GadgetHeight(Desktop())
If LoadIni=True Then
	ReadIni=OpenFile(".\SILL.ini")
	While Not Eof(ReadIni)
		FileInput$=ReadLine(ReadIni)
		Select ReadLoop
			Case 0
				ProgramVersion$=FileInput$
		
			Case 1
				PositionX=Right$(FileInput$,Len(FileInput$)-6)

			Case 2
				PositionY=Right$(FileInput$,Len(FileInput$)-6)

			Case 3
				MainSizeX=Right$(FileInput$,Len(FileInput$)-7)

			Case 4
				MainSizeY=Right$(FileInput$,Len(FileInput$)-7)
				
			Case 5
				Language$=Right$(FileInput$,Len(FileInput$)-10)+".lng"

		End Select
		ReadLoop=ReadLoop+1
	Wend
	CloseFile(ReadIni)
Else
	ProgramVersion$="[The Semi-Intelligent Language Learner - SILL - Version 2.0]"
	MainSizeX=640
	MainSizeY=200
	PositionX=(DesktopX/2)-(MainSizeX/2)
	PositionY=(DesktopY/2)-(MainSizeY/2)
	Language$="English.lng"
	DeleteFile ".\SILL.ini"
EndIf
LoadLanguage=CreateNewFile("lng\"+Language$)
If LoadLanguage=False Then CreateEnglish()
LoadLanguage()

;Creating the main program window
SILL_main=CreateWindow(Mid$(ProgramVersion$,2,Len(ProgramVersion$)-2)+FileName$,PositionX,PositionY,MainSizeX,MainSizeY,0)

;Creating and hiding the language selction window
SILL_lang=CreateWindow("Select Language",(DesktopX/2)-(320/2),(DesktopY/2)-(240/2),320,240,0,17)
HideGadget SILL_lang

;Create the programs menu
MainMenu=WindowMenu(SILL_main)
Include "menustrings.bb"
mnuFile=CreateMenu(mnuFileTxt$,0,MainMenu)
CreateMenu mnuFileReset,1,mnuFile
CreateMenu mnuFileWipe$,2,mnuFile
CreateMenu "",0,mnuFile
CreateMenu mnuFileQuitTxt$,3,mnuFile
mnuSettings=CreateMenu(mnuSettings$,0,MainMenu)
CreateMenu mnuSettingsLanguage$,4,mnuSettings
mnuTrain=CreateMenu(mnuTrain$,0,MainMenu)
CreateMenu mnuTrainGoodresponse,5,mnuTrain
CreateMenu mnuTrainBadresponse,6,mnuTrain
mnuHelp=CreateMenu(mnuHelp$,0,MainMenu)
CreateMenu mnuHelpHelp$,7,mnuHelp
CreateMenu "",0,mnuHelp
CreateMenu mnuHelpAbout$,8,mnuHelp
HotKeyEvent 59,0,$1001,7

;Populate main window
UserInput=CreateTextField(0,MainSizeY-100,MainSizeX-16,20,SILL_main)
Conversation=CreateTextArea(0,0,MainSizeX-16,MainSizeY-100,SILL_main,1)
btnInput=CreateButton("Enter",MainSizeX-120,MainSizeY-110,100,30,SILL_main,4)

;Populate language selection
Lang=CreateListBox(0,0,315,210,SILL_lang)
ListLanguage()

;Some final things to deal with before jumping into the main loop
HideGadget btnInput
SetMinWindowSize SILL_main,487,200
UpdateWindowMenu SILL_main
UpdateHistoryView()
AddTextAreaText Conversation,"------------------New conversation started------------------"+Chr$(13)
ActivateGadget UserInput

;Main loop
Repeat
	ID=WaitEvent()
	
	If ID=$801 Then
		PositionX=EventX()
		PositionY=EventY()
		SetStatusText SILL_main,"Pos: "+PositionX+" x "+PositionY
		Timed=0
	EndIf

	If ID=$401 Then
		If EventSource()=btnInput Then
			If Not Response$="" Then LearnFromUserResponse(Replace$(Response$,"?","$")+".txt")
			ProcessUserInput()
			ReplyToUser()
			ActivateGadget UserInput
			Timed=0
		EndIf
		If EventSource()=Lang Then
			Language$=GadgetItemText$(Lang,SelectedGadgetItem(Lang))+".lng"
			Notify "Restart SILL for changes to take effect!"
			HideGadget SILL_lang
		EndIf
	EndIf

	If ID=$802 Then
		ResizeWindow(EventX(),EventY())
		Timed=0
	EndIf
	
	If ID=$804
		ActivateGadget UserInput
	EndIf

	If ID=$803 Then
		If EventSource()=SILL_main Then Exit
		If EventSource()=SILL_lang Then HideGadget SILL_lang
	EndIf
	
	If ID=$1001 Then
		EID=EventData()
		Select EID
			Case 1
				ClearChat()
			
			Case 2
				WipeBrain()
				Timed=-2
			
			Case 3
				Exit
			
			Case 4
				ShowGadget SILL_lang
			
			Case 5
				MarkResponseGood()
			
			Case 6
				MarkResponseBad()
			
			Case 7
				ExecFile("SHViewer.exe")
			
			Case 8
				Notify "The Semi-Intelligent Language Learner"+Chr$(13)+Chr$(13)+"Copyright © 2018 - Gullspång Software Interactive"
			
		End Select
	EndIf
	
	If ID=$4001
		Timed=Timed+1
		If Timed=3 Then
			SetStatusText(SILL_main,"")
			;ActivateGadget UserInput
			;Timed=0
		EndIf
	EndIf
Forever

WriteIni()

End

Include "functions.bb"
Include "response.bb"