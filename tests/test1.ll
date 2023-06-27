; ModuleID = 'module'
source_filename = "module"

@sort_arr = global <5 x i32> zeroinitializer

define i32 @combine(ptr %0, i32 %1, ptr %2, i32 %3) {
combineEntry:
  %arr1 = alloca ptr, align 8
  store ptr %0, ptr %arr1, align 8
  %arr1_length = alloca i32, align 4
  store i32 %1, ptr %arr1_length, align 4
  %arr2 = alloca ptr, align 8
  store ptr %2, ptr %arr2, align 8
  %arr2_length = alloca i32, align 4
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
  %arr1_length2 = load i32, ptr %arr1_length, align 4
  %lt = icmp slt i32 %i1, %arr1_length2
  %zext_res = zext i1 %lt to i32
  %icmp = icmp ne i32 %zext_res, 0
  br i1 %icmp, label %andTrue, label %whileEnd

whileBody:                                        ; preds = %andTrue
  %i8 = load i32, ptr %i, align 4
  %res = getelementptr ptr, ptr %arr1, i32 0, i32 %i8
  %"arr1[i]" = load i32, ptr %res, align 4
  %j9 = load i32, ptr %j, align 4
  %res10 = getelementptr ptr, ptr %arr2, i32 0, i32 %j9
  %"arr2[j]" = load i32, ptr %res10, align 4
  %lt11 = icmp slt i32 %"arr1[i]", %"arr2[j]"
  %zext_res12 = zext i1 %lt11 to i32
  %icmp13 = icmp ne i32 %zext_res12, 0
  br i1 %icmp13, label %ifTrue, label %ifFalse

whileEnd:                                         ; preds = %andTrue, %whileCond
  %i32 = load i32, ptr %i, align 4
  %arr1_length33 = load i32, ptr %arr1_length, align 4
  %eq = icmp eq i32 %i32, %arr1_length33
  %zext_res34 = zext i1 %eq to i32
  %icmp35 = icmp ne i32 %zext_res34, 0
  br i1 %icmp35, label %ifTrue29, label %ifFalse30

andTrue:                                          ; preds = %whileCond
  %j3 = load i32, ptr %j, align 4
  %arr2_length4 = load i32, ptr %arr2_length, align 4
  %lt5 = icmp slt i32 %j3, %arr2_length4
  %zext_res6 = zext i1 %lt5 to i32
  %icmp7 = icmp ne i32 %zext_res6, 0
  br i1 %icmp7, label %whileBody, label %whileEnd

ifTrue:                                           ; preds = %whileBody
  %k14 = load i32, ptr %k, align 4
  %res15 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k14
  %i16 = load i32, ptr %i, align 4
  %res17 = getelementptr ptr, ptr %arr1, i32 0, i32 %i16
  %"arr1[i]18" = load i32, ptr %res17, align 4
  store i32 %"arr1[i]18", ptr %res15, align 4
  %i19 = load i32, ptr %i, align 4
  %add = add i32 %i19, 1
  store i32 %add, ptr %i, align 4
  br label %next

ifFalse:                                          ; preds = %whileBody
  %k20 = load i32, ptr %k, align 4
  %res21 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k20
  %j22 = load i32, ptr %j, align 4
  %res23 = getelementptr ptr, ptr %arr2, i32 0, i32 %j22
  %"arr2[j]24" = load i32, ptr %res23, align 4
  store i32 %"arr2[j]24", ptr %res21, align 4
  %j25 = load i32, ptr %j, align 4
  %add26 = add i32 %j25, 1
  store i32 %add26, ptr %j, align 4
  br label %next

next:                                             ; preds = %ifFalse, %ifTrue
  %k27 = load i32, ptr %k, align 4
  %add28 = add i32 %k27, 1
  store i32 %add28, ptr %k, align 4
  br label %whileCond

ifTrue29:                                         ; preds = %whileEnd
  br label %whileCond36

ifFalse30:                                        ; preds = %whileEnd
  br label %whileCond53

next31:                                           ; preds = %whileEnd55, %whileEnd38
  %arr1_length69 = load i32, ptr %arr1_length, align 4
  %arr2_length70 = load i32, ptr %arr2_length, align 4
  %add71 = add i32 %arr1_length69, %arr2_length70
  %sub = sub i32 %add71, 1
  %res72 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %sub
  %"sort_arr[arr1_length+arr2_length-1]" = load i32, ptr %res72, align 4
  ret i32 %"sort_arr[arr1_length+arr2_length-1]"

whileCond36:                                      ; preds = %whileBody37, %ifTrue29
  %j39 = load i32, ptr %j, align 4
  %arr2_length40 = load i32, ptr %arr2_length, align 4
  %lt41 = icmp slt i32 %j39, %arr2_length40
  %zext_res42 = zext i1 %lt41 to i32
  %icmp43 = icmp ne i32 %zext_res42, 0
  br i1 %icmp43, label %whileBody37, label %whileEnd38

whileBody37:                                      ; preds = %whileCond36
  %k44 = load i32, ptr %k, align 4
  %res45 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k44
  %j46 = load i32, ptr %j, align 4
  %res47 = getelementptr ptr, ptr %arr2, i32 0, i32 %j46
  %"arr2[j]48" = load i32, ptr %res47, align 4
  store i32 %"arr2[j]48", ptr %res45, align 4
  %k49 = load i32, ptr %k, align 4
  %add50 = add i32 %k49, 1
  store i32 %add50, ptr %k, align 4
  %j51 = load i32, ptr %j, align 4
  %add52 = add i32 %j51, 1
  store i32 %add52, ptr %j, align 4
  br label %whileCond36

whileEnd38:                                       ; preds = %whileCond36
  br label %next31

whileCond53:                                      ; preds = %whileBody54, %ifFalse30
  %i56 = load i32, ptr %i, align 4
  %arr1_length57 = load i32, ptr %arr1_length, align 4
  %lt58 = icmp slt i32 %i56, %arr1_length57
  %zext_res59 = zext i1 %lt58 to i32
  %icmp60 = icmp ne i32 %zext_res59, 0
  br i1 %icmp60, label %whileBody54, label %whileEnd55

whileBody54:                                      ; preds = %whileCond53
  %k61 = load i32, ptr %k, align 4
  %res62 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k61
  %i63 = load i32, ptr %i, align 4
  %res64 = getelementptr ptr, ptr %arr2, i32 0, i32 %i63
  %"arr2[i]" = load i32, ptr %res64, align 4
  store i32 %"arr2[i]", ptr %res62, align 4
  %k65 = load i32, ptr %k, align 4
  %add66 = add i32 %k65, 1
  store i32 %add66, ptr %k, align 4
  %i67 = load i32, ptr %i, align 4
  %add68 = add i32 %i67, 1
  store i32 %add68, ptr %i, align 4
  br label %whileCond53

whileEnd55:                                       ; preds = %whileCond53
  br label %next31
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
