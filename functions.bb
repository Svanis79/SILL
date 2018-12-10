;Functions of SILL version 2.0

;Main function to analyze the user input
Function ProcessUserInput()
	LeftToProcess$=TextFieldText$(UserInput)														;Taking the text from the text field.
	LeftToProcess$=Trim$(LeftToProcess$)															;Remove any leading or trailing spaces.
	EndChar$=Right$(LeftToProcess$,1)
	If Not EndChar$="." Or EndChar$="!" Or EndChar$="?" Then LeftToProcess$=LeftToProcess$+"."		;Checking to see if user ended sentence properly.
	WriteToFile("History\"+CurrentDate$()+".txt","["+CurrentTime$()+"] User: "+LeftToProcess$,0)	;Writing to the history file.
	AddTextAreaText Conversation,"["+CurrentTime()+"] User: "+LeftToProcess$+Chr$(13)
	FileName$=Replace$(LeftToProcess$,"?","$")+".txt"
;	Repeat
;		Char%=Char%+1
;		If Asc(Mid$(FileName$,Char%,1))>64 And Asc(Mid$(FileName$,Char%,1))<91 Or Asc(Mid$(FileName$,1))>140 Then FileName$=Left$(FileName$,Char%-1)+"_"+Right$(FileName$,Len(FileName$)-(Char%-1)):Char%=Char%+1
;	Until Char%=Len(FileName$)
	If Asc(Left$(FileName$,1))>64 And Asc(Left$(FileName$,1))<91 Or Asc(Left$(FileName$,1))>140 Then FileName$="_"+FileName$
	CreateNewFile("Brain\"+FileName$)																;Saving the sentence as a file.
	LeftToProcess$=Replace$(LeftToProcess$,"?"," $")												;Replace question marks with a dollar sign.
	LeftToProcess$=Replace$(LeftToProcess$,"!"," !")												;Replace exclamation marks with a dollar sign.
	LeftToProcess$=Replace$(LeftToProcess$,"."," .")												;Replace period with a dollar sign.
	LeftToProcess$=Replace$(LeftToProcess$,","," ,")												;Replace comma with a dollar sign.
	LeftToProcess$=LSet$(LeftToProcess$,Len(LeftToProcess$)+1)
	ReadFile=OpenFile(".\Brain\Words.txt")															;Open the Words.txt file to update it.
	Repeat
		SeekFile(ReadFile,0)																		;Go to the start of the file.
		Word$=Left$(LeftToProcess$,Instr(LeftToProcess$," ")-1)										;Locate the first word in the sentence and store it in its own variable.
		LeftToProcess$=Right$(LeftToProcess$,Len(LeftToProcess$)-Instr(LeftToProcess$," "))			;Delete that word from the sentence.
		Repeat
			SoL%=FilePos(ReadFile)
			WordFile$=ReadLine$(ReadFile)
			Number$=Mid(WordFile$,Instr(WordFile$,"~",1)+1,Len(WordFile$))
			WordFile$=Left(WordFile$,Len(WordFile$)-Len(WordFile$)+Instr(WordFile$,"~",1)-1)
			If Word$=WordFile$ Then
				Match=True
				CurrentWord%=Number$
				If Count%=0 Then
					Topic$=Word$
					LessUsedWord%=CurrentWord%+1
				EndIf
				Count%=Number$
				Count%=Count%+1
				Output$=Word$+"~"+Count%
				If SoL%=0 Then SoL%=-1
				WriteToFile("Words.txt",Output$,SoL%)
;				SeekFile(ReadFile,SoL%)
;				WriteLine(ReadFile,LSet$(Output$,64))
			EndIf
		Until Eof(ReadFile) Or Match=True
		If Match=False Then
			If LessUsedWord%>1 Then DeleteFile ".\Brain\Topics.txt"
			Count%=1
			LessUsedWord%=1
			WriteLine(ReadFile,LSet$(Word$+"~1",128))
		Else Match=False
		EndIf
		If Count%=LessUsedWord% Then
			CreateNewFile("Brain\Topics.txt")
			If Not Topic$="" Then WriteToFile("Topics.txt",Topic$,0)
			If Not Topic$=Word$ Then WriteToFile("Topics.txt",Word$,0)
			Topic$=""
		ElseIf Count%<LessUsedWord% Then
			Topic$=Word$
			LessUsedWord%=Count%
			DeleteFile ".\Brain\Topics.txt"
		EndIf
		If Asc(Left$(Word$,1))>64 And Asc(Left$(Word$,1))<91 Or Asc(Left$(Word$,1))>140 Then Word$="_"+Word$
		CreateNewFile("Brain\Pre-"+Word$+".txt")
		CreateNewFile("Brain\Pos-"+Word$+".txt")
		WordChainUpdate("Pre-"+Word$,PreWord$)
		If Not PreWord$="" Then WordChainUpdate("Pos-"+PreWord$,Word$)
		PreWord$=Word$
	Until LeftToProcess$=""
	CloseFile(ReadFile)																				;Don't forget to close Words.txt before leaving the function.
	SelectTopic(Topic$)
	If Asc(Left$(SelectedTopic$,1))>64 And Asc(Left$(SelectedTopic$,1))<91 Or Asc(Left$(SelectedTopic$,1))>140 Then TopicCorrection$="_"
	TopicFile=ReadFile(".\Brain\"+FileName$)
	Repeat
		TopicFromFile$=ReadLine(TopicFile)
		If Trim$(TopicFromFile$)="~"+TopicCorrection$+SelectedTopic$ Then TopicExists=True
	Until Eof(TopicFile) Or TopicExists=True
	If TopicExists=False Then WriteToFile(FileName$,"~"+TopicCorrection$+SelectedTopic$,0)
	SetStatusText SILL_main,"Guessing topic is: "+Chr$(34)+SelectedTopic$+Chr$(34)
	CloseFile(TopicFile)
End Function

;Function UpdateSentenseFile()
;	CheckFile=OpenFile(".\Brain\"+FileName$)
;	Repeat
;		FileLine$=ReadLine$(CheckFile)
;		If Match=False Then
;			CheckLine$="~"+SelectedTopic$
;			FileLine$=Trim$(FileLine$)
;			If CheckLine$=FileLine$ Then Match=True
;		EndIf
;	Until Eof(CheckFile)
;	If Match=False Then
;		If Not File$=Response$ Then WriteToFile(File$+".txt","~"+Left$(Topic$,Instr(Topic$," ")))
;		If File$=Response$ Then WriteToFile(File$+".txt","~"+Topic$)
;	EndIf
;	CloseFile(CheckFile)
;End Function

;After the words have been numbered according to how often they're used it's time to find what the topic of conversation is
Function SelectTopic(PassedTopic$)
	CreateNewFile("Brain\Topics.txt")
	SelectableTopics=OpenFile(".\Brain\Topics.txt")
	While Not Eof(SelectableTopics)
		ReadLine(SelectableTopics)
		NoTopics%=NoTopics%+1
	Wend
	If NoTopics%>0 Then
		SelectTopic%=Rnd(1,NoTopics%)
		SeekFile(SelectableTopics,0)
		For Count=1 To SelectTopic%
			SelectedTopic$=ReadLine(SelectableTopics)
		Next
	Else SelectedTopic$=PassedTopic$
	EndIf
	CloseFile(SelectableTopics)
	DeleteFile ".\Brain\Topics.txt"
	SelectedTopic$=Trim$(SelectedTopic$)
End Function

;Function to update a word that already exist
Function WordChainUpdate(ChainFile$,SearchWord$)
	SearchFile=OpenFile(".\Brain\"+ChainFile$+".txt")
	SeekFile(SearchFile,0)
	If Not SearchWord$="" Then Repeat
		SoL%=FilePos(SearchFile)
		If Not SearchWord$="" Then
			WordFound$=ReadLine(SearchFile)
			Number$=Mid(WordFound$,Instr(WordFound$,"~",1)+1,Len(WordFound$))
			WordFound$=Left(WordFound$,Len(WordFound$)-Len(WordFound$)+Instr(WordFound$,"~",1)-1)
			If SearchWord$=WordFound$ Then
				Found=True
				Count%=Number$
				Count%=Count%+1
				WriteToFile$=SearchWord$+"~"+Count%
				PrintOut=OpenFile(".\Brain\"+ChainFile$+".txt")
				SeekFile(PrintOut,SoL%)
				WriteLine(PrintOut,LSet$(WriteToFile$,128))
				CloseFile(PrintOut)
			EndIf
		EndIf
	Until Eof(SearchFile) Or Found=True
;	If it doesn't exist add it to the file
	If Found=False
		If Not SearchWord$="" WriteLine(SearchFile,LSet$(SearchWord$+"~1",128))
	Else Match=False
	EndIf
	CloseFile(SearchFile)
End Function

;Function to update the history file
Function WriteToFile(FileName$,PrintOut$,FilePosition%)
	WriteToFile=OpenFile(".\Brain\"+FileName$)
	If FilePosition%<>0 Then
		If FilePosition%=-1 Then FilePosition%=0
		SeekFile(WriteToFile,FilePosition%)
	Else
		Repeat
			ReadLine(WriteToFile)
		Until Eof(WriteToFile)
	EndIf
	WriteLine(WriteToFile,LSet$(PrintOut$,128))
	CloseFile(WriteToFile)
End Function

;Function to update the history with the what was just said
Function UpdateHistoryView()
	ReadHistory=OpenFile(".\Brain\History\"+CurrentDate$()+".txt")
	Repeat
		History$=History$+ReadLine(ReadHistory)+Chr$(13)
		SetTextAreaText Conversation,History$
	Until Eof(ReadHistory)
End Function

;Function to update size and position of main window and its gadgets
Function ResizeWindow(FunctionSizeX,FunctionSizeY)
	MainSizeX=FunctionSizeX
	MainSizeY=FunctionSizeY
	SetGadgetShape UserInput,0,MainSizeY-100,MainSizeX-16,20
	SetGadgetShape Conversation,0,0,MainSizeX-16,MainSizeY-100
	SetStatusText SILL_main,"Size: "+MainSizeX+" x "+MainSizeY
End Function

;Function to update the program .ini-file to store parameters
Function WriteIni()
	WriteIniFile=WriteFile(".\SILL.ini")
	WriteLine(WriteIniFile,"[The Semi-Intelligent Language Learner - SILL - Version 2.0]")
	WriteLine(WriteIniFile,"[PosX]"+Chr$(13)+PositionX)
	WriteLine(WriteIniFile,"[PosY]"+Chr$(13)+PositionY)
	WriteLine(WriteIniFile,"[SizeX]"+Chr$(13)+MainSizeX)
	WriteLine(WriteIniFile,"[SizeY]"+Chr$(13)+MainSizeY)
	WriteLine(WriteIniFile,"[Language]"+Chr$(13)+Left$(Language$,Len(Language$)-4))
	CloseFile(WriteIniFile)
End Function

;Function to clear chat if you don't want what you type to be considered an answer to what the AI just said
Function ClearChat()
	AddTextAreaText Conversation,"Current topic: "
	SelectedTopic$=""
	AddTextAreaText Conversation,"Cleared"+Chr$(13)
	AddTextAreaText Conversation,"Previous response: "
	Response$=""
	AddTextAreaText Conversation, "Cleared"+Chr$(13)
	AddTextAreaText Conversation, "Previously used file: "
	Fi1eName$=""
	AddTextAreaText Conversation, "Cleared"+Chr$(13)
	AddTextAreaText Conversation, "--- Chat Reset ---"+Chr$(13)+"------------------New conversation started------------------"+Chr$(13)
	WriteToFile("History\"+CurrentDate$()+".txt","--- Chat Reset ---",0)
End Function

;Function to allow user to select what language to use
Function ListLanguage()
	folder$=".\lng"
	myDir=ReadDir(folder$)
	Repeat
		file$=NextFile$(myDir)
		If file$="" Then Exit
		If FileType(folder$+"\"+file$) = 2 Then
;			Print "Folder:" + file$
		Else
;			Print "File:" + file$
			AddGadgetItem Lang,Left$(file$,Len(file$)-4)
		End If
	Forever
	CloseDir myDir
End Function

Function LoadLanguage()
	FileError=True
	LanguageFile=OpenFile(".\lng\"+Language$)
	While Not Eof(LanguageFile)
		MenuTxt$(LineNumber)=ReadLine(LanguageFile)
		LineNumber=LineNumber+1
	Wend
	CloseFile(LanguageFile)
	LoadedLanguage$=Mid$(MenuTxt$(0),2,(Len(MenuTxt$(0))-2))
	If LoadedLanguage$=Left$(Language$,Len(Language$)-4) Then FileError=False
End Function

;Function to create a new language file for English if none exists
Function CreateEnglish()
	English=WriteFile(".\lng\English.lng")
	WriteLine(English,"["+Left$(Language$,Len(Language$)-4)+"]")
	WriteLine(English,"mnuFile=&File")
	WriteLine(English,"mnuFileReset=&Reset Chat")
	WriteLine(English,"mnuFileWipe=&Wipe")
	WriteLine(English,"mnuFileQuit=&Quit")
	WriteLine(English,"mnuSettings=&Settings")
	WriteLine(English,"mnuSettingsLanguage=&Language")
	WriteLine(English,"mnuTrain=&Train")
	WriteLine(English,"mnuTrainGoodresponse=&Good Response")
	WriteLine(English,"mnuTrainBadresponse=&Bad Response")
	WriteLine(English,"mnuHelp=&Help")
	WriteLine(English,"mnuHelpHelp=Help")
	WriteLine(English,"mnuHelpAbout=&About")
	CloseFile(English)
End Function

;Function to create a Swedish language file
Function CreateSwedish()
	Swedish=WriteFile(".\lng\Swedish.lng")
	WriteLine(Swedish,"[Swedish]")
	WriteLine(Swedish,"mnuFile=&Arkiv")
	WriteLine(Swedish,"mnuFileReset=&Nollställ")
	WriteLine(Swedish,"mnuFileWipe=&Radera hjärnfiler")
	WriteLine(Swedish,"mnuFileQuit=&Avsluta")
	WriteLine(Swedish,"mnuSettings=&Inställningar")
	WriteLine(Swedish,"mnuSettingsLanguage=&Språk")
	WriteLine(Swedish,"mnuTrain=&Träna")
	WriteLine(Swedish,"mnuTrainGoodresponse=&Bra svar")
	WriteLine(Swedish,"mnuTrainBadresponse=&Dåligt svar")
	WriteLine(Swedish,"mnuHelp=&Hjälp")
	WriteLine(Swedish,"mnuHelpHelp=H&jälp")
	WriteLine(Swedish,"mnuHelpAbout=&Om SILL")
	CloseFile(Swedish)
End Function

;Function to check for existing file or create it if previously unexsisting
Function CreateNewFile(FileName$)
	CreateFile=OpenFile(".\"+FileName$)
	If CreateFile=0 Then
		CloseFile(CreateFile)
		CreateFile=WriteFile(".\"+FileName$)
	Else FileExist=1
	EndIf
	CloseFile(CreateFile)
	If FileExist=1 Then Return True
End Function

;Function to completely wipe the brain of SILL for a fresh start
Function WipeBrain()
	ReallyWipe=Confirm("This will wipe out the entire brain of the AI."+Chr$(13)+"You will not be able to undo this operation once it has begun. If you want to come back to your work, save the contents of the "+Chr$(34)+"Brain"+Chr$(34)+"-folder before you proceed."+Chr$(13)+Chr$(13)+"Are you absolutely SURE that this is what you want?")
	If ReallyWipe=1 Then
		SetStatusText SILL_main,"I'm afraid, Dave... my mind is going... I can feel it..."
		SetTextAreaText Conversation,"Now wiping out the brain..."+Chr$(13)
		Brain=ReadDir("./Brain")
		Repeat
			File$=NextFile(Brain)
			If File$="" Then Exit
			If Not FileType("./Brain"+"\"+File$)=2 Then
				DeleteFile "./Brain/"+File$
				AddTextAreaText Conversation,File$+" - Deleted"+Chr$(13)
			EndIf
		Forever
		CreateNewFile("Brain\Words.txt")
		AddTextAreaText Conversation,Chr$(13)
		AddTextAreaText Conversation,"Resetting memory of conversational topics: "
		SelectedTopic$=""
		AddTextAreaText Conversation,"Done"+Chr$(13)
		AddTextAreaText Conversation,"Resetting memory of my last response: "
		Response$=""
		AddTextAreaText Conversation,"Done"+Chr$(13)
		AddTextAreaText Conversation,"Entire brain deleted, you can now start teaching the AI from scratch."+Chr$(13)+"------------------New conversation started------------------"+Chr$(13)
		WriteToFile("History\"+CurrentDate$()+".txt","*** BRAIN DELETED ***",0)
	Else
		SetStatusText SILL_main,"Wiping of brain canceled."
	EndIf
End Function