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
  %arr19 = load ptr, ptr %arr1, align 8
  %res = getelementptr i32, ptr %arr19, i32 0, i32 %i8
  %"arr1[i]" = load i32, ptr %res, align 4
  %j10 = load i32, ptr %j, align 4
  %arr211 = load ptr, ptr %arr2, align 8
  %res12 = getelementptr i32, ptr %arr211, i32 0, i32 %j10
  %"arr2[j]" = load i32, ptr %res12, align 4
  %lt13 = icmp slt i32 %"arr1[i]", %"arr2[j]"
  %zext_res14 = zext i1 %lt13 to i32
  %icmp15 = icmp ne i32 %zext_res14, 0
  br i1 %icmp15, label %ifTrue, label %ifFalse

whileEnd:                                         ; preds = %andTrue, %whileCond
  %i36 = load i32, ptr %i, align 4
  %arr1_length37 = load i32, ptr %arr1_length, align 4
  %eq = icmp eq i32 %i36, %arr1_length37
  %zext_res38 = zext i1 %eq to i32
  %icmp39 = icmp ne i32 %zext_res38, 0
  br i1 %icmp39, label %ifTrue33, label %ifFalse34

andTrue:                                          ; preds = %whileCond
  %j3 = load i32, ptr %j, align 4
  %arr2_length4 = load i32, ptr %arr2_length, align 4
  %lt5 = icmp slt i32 %j3, %arr2_length4
  %zext_res6 = zext i1 %lt5 to i32
  %icmp7 = icmp ne i32 %zext_res6, 0
  br i1 %icmp7, label %whileBody, label %whileEnd

ifTrue:                                           ; preds = %whileBody
  %k16 = load i32, ptr %k, align 4
  %res17 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k16
  %i18 = load i32, ptr %i, align 4
  %arr119 = load ptr, ptr %arr1, align 8
  %res20 = getelementptr i32, ptr %arr119, i32 0, i32 %i18
  %"arr1[i]21" = load i32, ptr %res20, align 4
  store i32 %"arr1[i]21", ptr %res17, align 4
  %i22 = load i32, ptr %i, align 4
  %add = add i32 %i22, 1
  store i32 %add, ptr %i, align 4
  br label %next

ifFalse:                                          ; preds = %whileBody
  %k23 = load i32, ptr %k, align 4
  %res24 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k23
  %j25 = load i32, ptr %j, align 4
  %arr226 = load ptr, ptr %arr2, align 8
  %res27 = getelementptr i32, ptr %arr226, i32 0, i32 %j25
  %"arr2[j]28" = load i32, ptr %res27, align 4
  store i32 %"arr2[j]28", ptr %res24, align 4
  %j29 = load i32, ptr %j, align 4
  %add30 = add i32 %j29, 1
  store i32 %add30, ptr %j, align 4
  br label %next

next:                                             ; preds = %ifFalse, %ifTrue
  %k31 = load i32, ptr %k, align 4
  %add32 = add i32 %k31, 1
  store i32 %add32, ptr %k, align 4
  br label %whileCond

ifTrue33:                                         ; preds = %whileEnd
  br label %whileCond40

ifFalse34:                                        ; preds = %whileEnd
  br label %whileCond58

next35:                                           ; preds = %whileEnd60, %whileEnd42
  %arr1_length75 = load i32, ptr %arr1_length, align 4
  %arr2_length76 = load i32, ptr %arr2_length, align 4
  %add77 = add i32 %arr1_length75, %arr2_length76
  %sub = sub i32 %add77, 1
  %res78 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %sub
  ret ptr %res78

whileCond40:                                      ; preds = %whileBody41, %ifTrue33
  %j43 = load i32, ptr %j, align 4
  %arr2_length44 = load i32, ptr %arr2_length, align 4
  %lt45 = icmp slt i32 %j43, %arr2_length44
  %zext_res46 = zext i1 %lt45 to i32
  %icmp47 = icmp ne i32 %zext_res46, 0
  br i1 %icmp47, label %whileBody41, label %whileEnd42

whileBody41:                                      ; preds = %whileCond40
  %k48 = load i32, ptr %k, align 4
  %res49 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k48
  %j50 = load i32, ptr %j, align 4
  %arr251 = load ptr, ptr %arr2, align 8
  %res52 = getelementptr i32, ptr %arr251, i32 0, i32 %j50
  %"arr2[j]53" = load i32, ptr %res52, align 4
  store i32 %"arr2[j]53", ptr %res49, align 4
  %k54 = load i32, ptr %k, align 4
  %add55 = add i32 %k54, 1
  store i32 %add55, ptr %k, align 4
  %j56 = load i32, ptr %j, align 4
  %add57 = add i32 %j56, 1
  store i32 %add57, ptr %j, align 4
  br label %whileCond40

whileEnd42:                                       ; preds = %whileCond40
  br label %next35

whileCond58:                                      ; preds = %whileBody59, %ifFalse34
  %i61 = load i32, ptr %i, align 4
  %arr1_length62 = load i32, ptr %arr1_length, align 4
  %lt63 = icmp slt i32 %i61, %arr1_length62
  %zext_res64 = zext i1 %lt63 to i32
  %icmp65 = icmp ne i32 %zext_res64, 0
  br i1 %icmp65, label %whileBody59, label %whileEnd60

whileBody59:                                      ; preds = %whileCond58
  %k66 = load i32, ptr %k, align 4
  %res67 = getelementptr <5 x i32>, ptr @sort_arr, i32 0, i32 %k66
  %i68 = load i32, ptr %i, align 4
  %arr269 = load ptr, ptr %arr2, align 8
  %res70 = getelementptr i32, ptr %arr269, i32 0, i32 %i68
  %"arr2[i]" = load i32, ptr %res70, align 4
  store i32 %"arr2[i]", ptr %res67, align 4
  %k71 = load i32, ptr %k, align 4
  %add72 = add i32 %k71, 1
  store i32 %add72, ptr %k, align 4
  %i73 = load i32, ptr %i, align 4
  %add74 = add i32 %i73, 1
  store i32 %add74, ptr %i, align 4
  br label %whileCond58

whileEnd60:                                       ; preds = %whileCond58
  br label %next35
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
