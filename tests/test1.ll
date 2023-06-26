; ModuleID = 'module'
source_filename = "module"

@sort_arr = global <5 x i32> zeroinitializer

define i32 @combine(ptr %0, i32 %1, ptr %2, i32 %3) {
combineEntry:
  %arr1 = alloca ptr, align 8
  store ptr %0, ptr %arr1, align 8
  %arr1_length = alloca ptr, align 8
  store i32 %1, ptr %arr1_length, align 4
  %arr2 = alloca ptr, align 8
  store ptr %2, ptr %arr2, align 8
  %arr2_length = alloca ptr, align 8
  store i32 %3, ptr %arr2_length, align 4
  %i = alloca i32, align 4
  store i32 0, ptr %i, align 4
  %j = alloca i32, align 4
  store i32 0, ptr %j, align 4
  %k = alloca i32, align 4
  store i32 0, ptr %k, align 4
  br label %whileCond

whileCond:                                        ; preds = %next, %combineEntry
  %i1 = load i32, ptr %i, align 4
  %lt = icmp slt i32 %i1, ptr %arr1_length
  %zext_res = zext i1 %lt to i32
  %icmp = icmp ne i32 %zext_res, 0
  br i1 %icmp, label %andTrue, label %whileEnd

whileBody:                                        ; preds = %andTrue
  %i6 = load i32, ptr %i, align 4
  %res = getelementptr ptr, ptr %arr1, i32 0, i32 %i6
  %"arr1[i]" = load i32, ptr %res, align 4
  %j7 = load i32, ptr %j, align 4
  %res8 = getelementptr ptr, ptr %arr2, i32 0, i32 %j7
  %"arr2[j]" = load i32, ptr %res8, align 4
  %lt9 = icmp slt i32 %"arr1[i]", %"arr2[j]"
  %zext_res10 = zext i1 %lt9 to i32
  %icmp11 = icmp ne i32 %zext_res10, 0
  br i1 %icmp11, label %ifTrue, label %ifFalse

whileEnd:                                         ; preds = %andTrue, %whileCond
  %i30 = load i32, ptr %i, align 4
  %eq = icmp eq i32 %i30, ptr %arr1_length
  %zext_res31 = zext i1 %eq to i32
  %icmp32 = icmp ne i32 %zext_res31, 0
  br i1 %icmp32, label %ifTrue27, label %ifFalse28

andTrue:                                          ; preds = %whileCond
  %j2 = load i32, ptr %j, align 4
  %lt3 = icmp slt i32 %j2, ptr %arr2_length
  %zext_res4 = zext i1 %lt3 to i32
  %icmp5 = icmp ne i32 %zext_res4, 0
  br i1 %icmp5, label %whileBody, label %whileEnd

ifTrue:                                           ; preds = %whileBody
  %k12 = load i32, ptr %k, align 4
  %res13 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k12
  %i14 = load i32, ptr %i, align 4
  %res15 = getelementptr ptr, ptr %arr1, i32 0, i32 %i14
  %"arr1[i]16" = load i32, ptr %res15, align 4
  store i32 %"arr1[i]16", ptr %res13, align 4
  %i17 = load i32, ptr %i, align 4
  %add = add i32 %i17, 1
  store i32 %add, ptr %i, align 4
  br label %next

ifFalse:                                          ; preds = %whileBody
  %k18 = load i32, ptr %k, align 4
  %res19 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k18
  %j20 = load i32, ptr %j, align 4
  %res21 = getelementptr ptr, ptr %arr2, i32 0, i32 %j20
  %"arr2[j]22" = load i32, ptr %res21, align 4
  store i32 %"arr2[j]22", ptr %res19, align 4
  %j23 = load i32, ptr %j, align 4
  %add24 = add i32 %j23, 1
  store i32 %add24, ptr %j, align 4
  br label %next

next:                                             ; preds = %ifFalse, %ifTrue
  %k25 = load i32, ptr %k, align 4
  %add26 = add i32 %k25, 1
  store i32 %add26, ptr %k, align 4
  br label %whileCond

ifTrue27:                                         ; preds = %whileEnd
  br label %whileCond33

ifFalse28:                                        ; preds = %whileEnd
  br label %whileCond49

next29:                                           ; preds = %whileEnd51, %whileEnd35
  %add64 = add ptr %arr1_length, %arr2_length
  %sub = sub ptr %add64, i32 1
  %res65 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, ptr %sub
  %"sort_arr[arr1_length+arr2_length-1]" = load i32, ptr %res65, align 4
  ret i32 %"sort_arr[arr1_length+arr2_length-1]"

whileCond33:                                      ; preds = %whileBody34, %ifTrue27
  %j36 = load i32, ptr %j, align 4
  %lt37 = icmp slt i32 %j36, ptr %arr2_length
  %zext_res38 = zext i1 %lt37 to i32
  %icmp39 = icmp ne i32 %zext_res38, 0
  br i1 %icmp39, label %whileBody34, label %whileEnd35

whileBody34:                                      ; preds = %whileCond33
  %k40 = load i32, ptr %k, align 4
  %res41 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k40
  %j42 = load i32, ptr %j, align 4
  %res43 = getelementptr ptr, ptr %arr2, i32 0, i32 %j42
  %"arr2[j]44" = load i32, ptr %res43, align 4
  store i32 %"arr2[j]44", ptr %res41, align 4
  %k45 = load i32, ptr %k, align 4
  %add46 = add i32 %k45, 1
  store i32 %add46, ptr %k, align 4
  %j47 = load i32, ptr %j, align 4
  %add48 = add i32 %j47, 1
  store i32 %add48, ptr %j, align 4
  br label %whileCond33

whileEnd35:                                       ; preds = %whileCond33
  br label %next29

whileCond49:                                      ; preds = %whileBody50, %ifFalse28
  %i52 = load i32, ptr %i, align 4
  %lt53 = icmp slt i32 %i52, ptr %arr1_length
  %zext_res54 = zext i1 %lt53 to i32
  %icmp55 = icmp ne i32 %zext_res54, 0
  br i1 %icmp55, label %whileBody50, label %whileEnd51

whileBody50:                                      ; preds = %whileCond49
  %k56 = load i32, ptr %k, align 4
  %res57 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k56
  %i58 = load i32, ptr %i, align 4
  %res59 = getelementptr ptr, ptr %arr2, i32 0, i32 %i58
  %"arr2[i]" = load i32, ptr %res59, align 4
  store i32 %"arr2[i]", ptr %res57, align 4
  %k60 = load i32, ptr %k, align 4
  %add61 = add i32 %k60, 1
  store i32 %add61, ptr %k, align 4
  %i62 = load i32, ptr %i, align 4
  %add63 = add i32 %i62, 1
  store i32 %add63, ptr %i, align 4
  br label %whileCond49

whileEnd51:                                       ; preds = %whileCond49
  br label %next29
}

define i32 @main() {
mainEntry:
  %a = alloca <2 x i32>, align 8
  %pointer = getelementptr <2 x i32>, ptr %a, i32 0, i32 0
  store i32 1, ptr %pointer, align 4
  %pointer1 = getelementptr <2 x i32>, ptr %a, i32 0, i32 1
  store i32 5, ptr %pointer1, align 4
  %b = alloca <3 x i32>, align 16
  %pointer2 = getelementptr <3 x i32>, ptr %b, i32 0, i32 0
  store i32 1, ptr %pointer2, align 4
  %pointer3 = getelementptr <3 x i32>, ptr %b, i32 0, i32 1
  store i32 4, ptr %pointer3, align 4
  %pointer4 = getelementptr <3 x i32>, ptr %b, i32 0, i32 2
  store i32 14, ptr %pointer4, align 4
  %res = getelementptr <2 x i32>, ptr %a, i32 0, i32 0
  %res5 = getelementptr <3 x i32>, ptr %b, i32 0, i32 0
  %returnValue = call i32 @combine(ptr %res, i32 2, ptr %res5, i32 3)
  ret i32 %returnValue
}
