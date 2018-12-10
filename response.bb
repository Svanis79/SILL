;Possibility$="them~356"
;UseRate%=Right$(Possibility$,Len(Possibility$)-Instr(Possibility$,"~"))
;Print Left$(Possibility$,Instr(Possibility$,"~")-1)
;Print UseRate%
;Stop

;Function that finds a reply for what the user said
Function ReplyToUser()
	UserSaid$=TextFieldText$(UserInput)
	If Asc(Left$(SelectedTopic$,1))>64 And Asc(Left$(SelectedTopic$,1))<91 Or Asc(Left$(SelectedTopic$,1))>140 Then TopicCorrection$="_"
	;First, let's see if we've had this conversation before and so already have a good response ready
	Responses=ReadFile(".\Brain\"+FileName$)
	Repeat
		LineNo%=LineNo%+1
		TopicFromFile$=ReadLine(Responses)
		If Trim$(TopicFromFile$)="~"+TopicCorrection$+SelectedTopic$ Then TopicFound=True
	Until Eof(Responses) Or TopicFound=True
	Repeat
		LineNo%=LineNo%+1
		MarkedBad=False
		ReplyFromFile$=ReadLine(Responses)
		If Not Trim$(ReplyFromFile$)="" Then
			If Trim$(Left$(ReplyFromFile$,5))="[BAD]" Then MarkedBad=True
			If Trim$(Left$(ReplyFromFile$,1))<>"~" And FirstReply%=0 And MarkedBad=False Then FirstReply%=LineNo%
			If Trim$(Left$(ReplyFromFile$,1))<>"~" And FirstReply%<>0 And MarkedBad=False Then LastReply%=LineNo%
			If Trim$(Left$(ReplyFromFile$,1))="~" And LastReply%<>0 Then
				If FirstReply%=LastReply% Then SelectReply%=FirstReply%
				If FirstReply%<>LastReply% Then SelectReply%=Rnd(FirstReply%,LastReply%)
			EndIf
		ElseIf Trim$(ReplyFromFile$)="" Then
			If FirstReply%=LastReply% Then SelectReply%=FirstReply%
			If FirstReply%<>LastReply% Then SelectReply%=Rnd(FirstReply%,LastReply%)
		EndIf
		If MarkedBad=True Then BadFlag=True
		If SelectReply%<>0 Then
			SeekFile(Responses,0)
			For Answer%=1 To SelectReply%
			Response$=ReadLine(Responses)
			Next
			ReplyFound=True
		EndIf
	Until Eof(Responses) And LineNo%>LastReply%+1 Or ReplyFound=True
	CloseFile(Responses)
	If ReplyFound=True Then
		WriteToFile("History\"+CurrentDate$()+".txt","["+CurrentTime$()+"] SILL: "+Response$,0)
		AddTextAreaText Conversation,"["+CurrentTime()+"] SILL: "+Response$+Chr$(13)
		SetGadgetText UserInput,""
	Else
		;If all else fails, generate a new response
		GenerateResponse(BadFlag)
	EndIf
End Function

;Function to mark a response as good
Function MarkResponseGood()
	UserSaid=ReadFile(".\Brain\"+FileName$)
	If Asc(Left$(SelectedTopic$,1))>64 And Asc(Left$(SelectedTopic$,1))<91 Or Asc(Left$(SelectedTopic$,1))>140 Then TopicCorrection$="_"
	Repeat
		LineNo%=LineNo%+1
		TopicFromFile$=ReadLine(UserSaid)
		If Trim$(TopicFromFile$)="~"+TopicCorrection$+SelectedTopic$ Then TopicFound=True
	Until Eof(UserSaid) Or TopicFound=True
	Repeat
		StoredResponse$=ReadLine(UserSaid)
		If Trim$(Left$(StoredResponse$,5))="[BAD]" Then LineNo%=LineNo%+1
		If Trim$(Left$(StoredResponse$,1))="~" Then TooLate=True
		If Trim$(StoredResponse$)=Response$ And TooLate=False Then Noted=True
	Until Eof(UserSaid) Or Noted=True
	If TopicFound=True Then
		If Noted=False Then
			TempFile=WriteFile(SystemProperty ("tempdir")+"SILLtemp.txt")
			SeekFile(UserSaid,0)
			For Line%=1 To LineNo%
				PreUpdate$=ReadLine(UserSaid)
				If Not PreUpdate$="" Then WriteLine(TempFile,Trim$(PreUpdate$))
			Next
			WriteLine(TempFile,Response$)
			Repeat
				PosUpdate$=ReadLine(UserSaid)
				If Not PosUpdate$="" Then WriteLine(TempFile,Trim$(PosUpdate$))
			Until Eof(UserSaid)
		EndIf
	Else Notify "Something is wrong. I was unable to locate the topic we were just discussing and, as a result, am unable to store my response as being good."
	EndIf
	CloseFile(TempFile)
	CloseFile(UserSaid)
	CopyFile(SystemProperty ("tempdir")+"SILLtemp.txt",".\Brain\"+FileName$)
	DeleteFile SystemProperty ("tempdir")+"SILLtemp.txt"
	RewriteHistory=OpenFile(".\Brain\History\"+CurrentDate$()+".txt")
	Repeat
		Revisit%=FilePos(RewriteHistory)
		PreviousResponse$=ReadLine(RewriteHistory)
	Until Eof(RewriteHistory)
	SeekFile(RewriteHistory,Revisit%)
	WriteLine(RewriteHistory,LSet$(Trim$(PreviousResponse$)+" <<< [GOOD]",128))
	CloseFile(RewriteHistory)
	SetTextAreaText Conversation,Trim$(PreviousResponse$)+" <<< Response marked [GOOD]!"+Chr$(13),TextAreaLen(Conversation,2)-2,TextAreaLen(Conversation,2),2
End Function

;Function to mark a response as bad
Function MarkResponseBad()
	UserSaid=ReadFile(".\Brain\"+FileName$)
	Repeat
		LineNo%=LineNo%+1
		TopicFromFile$=ReadLine(UserSaid)
		If Trim$(TopicFromFile$)="~"+SelectedTopic$ Then TopicFound=True
	Until Eof(UserSaid) Or TopicFound=True
	Repeat
		StoredResponse$=ReadLine(UserSaid)
		If Trim$(StoredResponse$)=Response$ Then Noted=True
	Until Eof(UserSaid) Or Noted=True
	If TopicFound=True Then
		If Noted=False Then
			TempFile=WriteFile(SystemProperty ("tempdir")+"SILLtemp.txt")
			SeekFile(UserSaid,0)
			For Line%=1 To LineNo%
				PreUpdate$=ReadLine(UserSaid)
				If Not PreUpdate$="" Then WriteLine(TempFile,Trim$(PreUpdate$))
			Next
			WriteLine(TempFile,"[BAD]"+Response$)
			Repeat
				PosUpdate$=ReadLine(UserSaid)
				If Not PosUpdate$="" Then WriteLine(TempFile,Trim$(PosUpdate$))
			Until Eof(UserSaid)
		EndIf
	Else Notify "Something is wrong. I was unable to locate the topic we were just discussing and, as a result, am unable to store my response as being bad."
	EndIf
	CloseFile(TempFile)
	CloseFile(UserSaid)
	CopyFile(SystemProperty ("tempdir")+"SILLtemp.txt",".\Brain\"+FileName$)
	DeleteFile SystemProperty ("tempdir")+"SILLtemp.txt"
	RewriteHistory=ReadFile(".\Brain\History\"+CurrentDate$()+".txt")
	Repeat
		Revisit%=FilePos(RewriteHistory)
		PreviousResponse$=ReadLine(RewriteHistory)
	Until Eof(RewriteHistory)
	SeekFile(RewriteHistory,Revisit%)
	WriteLine(RewriteHistory,LSet$(Trim$(PreviousResponse$)+" <<< [BAD]",128))
	CloseFile(RewriteHistory)
	SetTextAreaText Conversation,Trim$(PreviousResponse$)+" <<< Response marked [BAD]!"+Chr$(13),TextAreaLen(Conversation,2)-2,TextAreaLen(Conversation,2),2
	ClearChat()
End Function

;Function to add users reply as a suggested response to what I said
Function LearnFromUserResponse(GetFile$)
	GetFile$=Replace$(Response$,"?","$")+".txt"
	If Asc(Left$(GetFile$,1))>64 And Asc(Left$(GetFile$,1))<91 Or Asc(Left$(GetFile$,1))>140 Then GetFile$="_"+GetFile$
	UserSaid$=TextFieldText$(UserInput)
	UserResponse=ReadFile(".\Brain\"+GetFile$)
	If UserResponse=0 Then
		RuntimeError "Something went wrong when trying to open file: "+Chr$(13)+GetFile$
		Return
	EndIf
	Repeat
		LineNo%=LineNo%+1
		TopicFromFile$=ReadLine(UserResponse)
		If Trim$(TopicFromFile$)="~"+SelectedTopic$ Then TopicFound=True
	Until Eof(UserResponse) Or TopicFound=True
	Repeat
		StoredResponse$=ReadLine(UserResponse)
		If Trim$(Left$(StoredResponse$,5))="[BAD]" Then LineNo%=LineNo%+1
		If Trim$(Left$(StoredResponse$,1))="~" Then TooLate=True
		If Trim$(StoredResponse$)=Trim$(UserSaid$) And TooLate=False Then Noted=True
	Until Eof(UserResponse) Or Noted=True
	If Noted=False Then
		TempFile=WriteFile(SystemProperty ("tempdir")+"SILLtemp.txt")
		SeekFile(UserResponse,0)
		For Line%=1 To LineNo%
			PreUpdate$=ReadLine(UserResponse)
			If Not PreUpdate$="" Then WriteLine(TempFile,Trim$(PreUpdate$))
		Next
		WriteLine(TempFile,UserSaid$)
		Repeat
			PosUpdate$=ReadLine(UserResponse)
			If Not PosUpdate$="" Then WriteLine(TempFile,Trim$(PosUpdate$))
		Until Eof(UserResponse)
	EndIf
	CloseFile(TempFile)
	CloseFile(UserResponse)
	CopyFile(SystemProperty ("tempdir")+"SILLtemp.txt",".\Brain\"+GetFile$)
	DeleteFile SystemProperty ("tempdir")+"SILLtemp.txt"
End Function

Function AddMyResponse(FileToAdd$,Topic$)
	If Asc(Mid$(FileToAdd$,7,1))>64 And Asc(Mid$(FileToAdd$,7,1))<91 Or Asc(Mid$(FileToAdd$,7,1))>140 Then FileToAdd$="Brain\"+"_"+Right$(FileToAdd$,Len(FileToAdd$)-6)
	CreateNewFile(FileToAdd$)
	MyResponse=OpenFile(".\"+FileToAdd$)
	If MyResponse=0 Then
		RuntimeError "Something went wrong when trying to open file: "+Chr$(13)+FileToAdd$
		Return
	EndIf
	Repeat
		TopicFromFile$=ReadLine(MyResponse)
		If Trim$(TopicFromFile$)=Topic$ Then TopicExist=True
	Until Eof(MyResponse) Or TopicExist=True
	CloseFile(MyResponse)
	If TopicExist=False Then WriteToFile(Right$(FileToAdd$,Len(FileToAdd$)-6),Topic$,0)
End Function

Function GenerateResponse(BadFlag)
	Response$=SelectedTopic$+" "
	If Asc(Left$(SelectedTopic$,1))>64 And Asc(Left$(SelectedTopic$,1))<91 Or Asc(Left$(SelectedTopic$,1))>140 Then SelectedTopic$="_"+SelectedTopic$
	File$="Pos-"+SelectedTopic$+".txt"
	Repeat
		Loop%=Loop%+1
		GenerateResponse=OpenFile(".\Brain\"+File$)
			Repeat
				Possibility$=ReadLine(GenerateResponse)
				UseRate%=Right$(Possibility$,Len(Possibility$)-Instr(Possibility$,"~"))
				If Possibility$="" Then Done=True
				If UseRate%>Current% Then
					If Not CreateFile=0 Then CloseFile(CreateFile):CreateFile=0
					DeleteFile SystemProperty ("tempdir")+"SILLtemp.txt"
					Current%=UseRate%
					Suggested$=Left$(Possibility$,Instr(Possibility$,"~")-1)
					Memory$=Suggested$
				ElseIf UseRate%=Current% And Possibility$<>""
					If CreateFile=0 Then CreateFile=WriteFile(SystemProperty ("tempdir")+"SILLtemp.txt")
					If Memory$<>"" Then
						WriteLine(CreateFile,Memory$)
						Memory$=""
					EndIf
					WriteLine(CreateFile,Left$(Possibility$,Instr(Possibility$,"~")-1))
					CloseFile(CreateFile):CreateFile=0
				EndIf
			Until Eof(GenerateResponse)
			If Not CreateFile=0 Then CloseFile(CreateFile):CreateFile=0
			CreateFile=OpenFile(SystemProperty ("tempdir")+"SILLtemp.txt")
			If Not CreateFile=0 Then
				Suggestions%=0
				While Not Eof(CreateFile)
					ReadLine(CreateFile)
					Suggestions%=Suggestions%+1
				Wend
				Selected%=Rnd(1,Suggestions%)
				SeekFile(CreateFile,0)
				For Count% = 1 To Selected%
					Suggested$=ReadLine(CreateFile)
				Next
				CloseFile(CreateFile):CreateFile=0
			EndIf
		CloseFile(GenerateResponse)
		If Not Done=True Then
			File$="Pos-"+Suggested$+".txt"
			If BadFlag=True And BackupTopic$="" Then BackupTopic$=Suggested$
			Response$=Response$+Suggested$+" "
		EndIf
		Suggested$=""
		Current%=0
		UseRate%=0
	Until Done=True Or Loop%=100
	If Not CloseFile=0 Then CloseFile(CreateFile)
	DeleteFile SystemProperty ("tempdir")+"SILLtemp.txt"
	File$="Pre-"+SelectedTopic$+".txt"
	Done=False
	Repeat
		Loop%=Loop%+1
		GenerateResponse=OpenFile(".\Brain\"+File$)
			Repeat
				Possibility$=ReadLine(GenerateResponse)
				UseRate%=Right$(Possibility$,Len(Possibility$)-Instr(Possibility$,"~"))
				If Possibility$="" Then Done=True
				If UseRate%>Current% Then
					If Not CreateFile=0 Then CloseFile(CreateFile):CreateFile=0
					DeleteFile SystemProperty ("tempdir")+"SILLtemp.txt"
					Current%=UseRate%
					Suggested$=Left$(Possibility$,Instr(Possibility$,"~")-1)
					Memory$=Suggested$
				ElseIf UseRate%=Current% And Possibility$<>""
					If CreateFile=0 Then CreateFile=WriteFile(SystemProperty ("tempdir")+"SILLtemp.txt")
					If Memory$<>"" Then
						WriteLine(CreateFile,Memory$)
						Memory$=""
					EndIf
					WriteLine(CreateFile,Left$(Possibility$,Instr(Possibility$,"~")-1))
					CloseFile(CreateFile):CreateFile=0
				EndIf
			Until Eof(GenerateResponse)
			If Not CloseFile=0 Then CloseFile(CreateFile)
			CreateFile=OpenFile(SystemProperty ("tempdir")+"SILLtemp.txt")
			If Not CreateFile=0 Then
				Suggestions%=0
				While Not Eof(CreateFile)
					ReadLine(CreateFile)
					Suggestions%=Suggestions%+1
				Wend
				Selected%=Rnd(1,Suggestions%)
				SeekFile(CreateFile,0)
				For Count% = 1 To Selected%
					Suggested$=ReadLine(CreateFile)
				Next
				CloseFile(CreateFile):CreateFile=0
			EndIf
		CloseFile(GenerateResponse)
		If Not Done=True Then
			File$="Pre-"+Suggested$+".txt"
			If BadFlag=True And BackupTopic$="" Then BackupTopic$=Suggested$
			Response$=Suggested$+" "+Response$
			Suggested$=""
			Current%=0
			UseRate%=0
		EndIf
	Until Done=True Or Loop%=100
	If Loop%=100 Then Notify "I seem to have gotten stuck in an infinite loop!"+Chr$(13)+Chr$(13)+"To see what happened, please refer to the file Error.log.",True:DebugLog "Error in function "+Chr$(34)+"GenerateResponse"+Chr$(34)+", filename ended up being: Brain\"+Replace$(Response$,"?","$")+".txt"+Chr$(13)+"Skipping save to solve..."
	If Not CloseFile=0 Then CloseFile(CreateFile)
	DeleteFile SystemProperty ("tempdir")+"SILLtemp.txt"
	Response$=Trim$(Response$)
	Response$=Replace$(Response$," _"," ")
	Response$=Replace$(Response$," $","?")
	Response$=Replace$(Response$," !","!")
	Response$=Replace$(Response$," .",".")
	Response$=Replace$(Response$," ,",",")
	If BadFlag=True Then ResponseBad=CheckGeneratedResponse()
	If ResponseBad=True Then
		Topic$=SelectedTopic$
		SelectedTopic$=BackupTopic$
		GenerateResponse(BadFlag)
;		SelectedTopic$=Topic$
		Return
	Else
		If Len(Response$)>255 Then
			CreateNewFile("Error.log")
			ErrorLog=OpenFile(".\Error.log")
			NameLength%=Len(Replace$(Response$,"?","$")+".txt")
			Repeat
				ReadLine(ErrorLog)
			Until Eof(ErrorLog)
			WriteLine(ErrorLog,"--- "+CurrentDate()+" ---")
			WriteLine(ErrorLog,"As a result of being stuck in an infinite loop, I tried to create the file: "+Replace$(Response$,"?","$")+".txt")
			WriteLine(ErrorLog,"This would be a filename of "+NameLength%+" characters, thus exceeding the limitations of NTFS by "+(NameLength%-255)+" characters.")
			WriteLine(errorlog,"")
			WriteLine(ErrorLog,"Make a note of the random seed being: "+RndSeed()+" and include that with the history files if you want to inform the developer of this.")
			CloseFile(ErrorLog)
			SetStatusText SILL_main,"File error caused by infinite loop. Random seed: "+RndSeed()
			FileError=True
		EndIf
		If FileError=False Then AddMyResponse("Brain\"+Replace$(Response$,"?","$")+".txt","~"+SelectedTopic$)
		If Left$(Response$,1)="_" Then Response$=Right$(Response$,Len(Response$)-1)
		WriteToFile("History\"+CurrentDate$()+".txt","["+CurrentTime$()+"] SILL: "+Response$,0)
		AddTextAreaText Conversation,"["+CurrentTime()+"] SILL: "+Response$+Chr$(13)
		SetGadgetText UserInput,""
	EndIf
End Function

;Funktion to check if a generated response is OK or already marked as a bad response
Function CheckGeneratedResponse()
	GeneratedResponse=ReadFile(".\Brain\"+FileName$)
	Repeat
		TopicFromFile$=ReadLine(GeneratedResponse)
		If Trim$(TopicFromFile$)="~"+SelectedTopic$ Then TopicFound=True
	Until Eof(GeneratedResponse) Or TopicFound=True
	Repeat
		ReadResponse$=ReadLine(GeneratedResponse)
		If ReadResponse$="[BAD]"+Response$ Then BadConfirmed=True
	Until Eof(GeneratedResponse) Or BadConfirmed=True Or Left$(ReadResponse$,1)="~"
	CloseFile(GenerateResponse)
	If BadConfirmed=True Then
		Return True
	Else
		Return False
	EndIf
End Function