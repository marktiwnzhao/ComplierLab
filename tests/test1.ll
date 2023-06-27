; ModuleID = 'module'
source_filename = "module"

define i32 @baka_add(ptr %0, i32 %1, i32 %2) {
baka_addEntry:
  %a = alloca ptr, align 8
  store ptr %0, ptr %a, align 8
  %i = alloca i32, align 4
  store i32 %1, ptr %i, align 4
  %j = alloca i32, align 4
  store i32 %2, ptr %j, align 4
  %i1 = load i32, ptr %i, align 4
  %a2 = load ptr, ptr %a, align 8
  %res = getelementptr i32, ptr %a2, i32 0, i32 %i1
  %"a[i]" = load i32, ptr %res, align 4
  %j3 = load i32, ptr %j, align 4
  %a4 = load ptr, ptr %a, align 8
  %res5 = getelementptr i32, ptr %a4, i32 0, i32 %j3
  %"a[j]" = load i32, ptr %res5, align 4
  %add = add i32 %"a[i]", %"a[j]"
  %ge = icmp sge i32 %add, 10
  %zext_res = zext i1 %ge to i32
  %icmp = icmp ne i32 %zext_res, 0
  br i1 %icmp, label %ifTrue, label %ifFalse

ifTrue:                                           ; preds = %baka_addEntry
  ret i32 9
  br label %next

ifFalse:                                          ; preds = %baka_addEntry
  %i6 = load i32, ptr %i, align 4
  %a7 = load ptr, ptr %a, align 8
  %res8 = getelementptr i32, ptr %a7, i32 0, i32 %i6
  %"a[i]9" = load i32, ptr %res8, align 4
  %j10 = load i32, ptr %j, align 4
  %a11 = load ptr, ptr %a, align 8
  %res12 = getelementptr i32, ptr %a11, i32 0, i32 %j10
  %"a[j]13" = load i32, ptr %res12, align 4
  %add14 = add i32 %"a[i]9", %"a[j]13"
  ret i32 %add14
  br label %next

next:                                             ; preds = %ifFalse, %ifTrue
  ret i32 0
}

define i32 @kyu(ptr %0, i32 %1) {
kyuEntry:
  %a = alloca ptr, align 8
  store ptr %0, ptr %a, align 8
  %n = alloca i32, align 4
  store i32 %1, ptr %n, align 4
  %i = alloca i32, align 4
  store i32 0, ptr %i, align 4
  br label %whileCond

whileCond:                                        ; preds = %whileBody, %kyuEntry
  %i1 = load i32, ptr %i, align 4
  %n2 = load i32, ptr %n, align 4
  %lt = icmp slt i32 %i1, %n2
  %zext_res = zext i1 %lt to i32
  %icmp = icmp ne i32 %zext_res, 0
  br i1 %icmp, label %whileBody, label %whileEnd

whileBody:                                        ; preds = %whileCond
  %i3 = load i32, ptr %i, align 4
  %a4 = load ptr, ptr %a, align 8
  %res = getelementptr i32, ptr %a4, i32 0, i32 %i3
  %res5 = load ptr, ptr %a, align 8
  %i6 = load i32, ptr %i, align 4
  %i7 = load i32, ptr %i, align 4
  %add = add i32 %i7, 1
  %n8 = load i32, ptr %n, align 4
  %mod = srem i32 %add, %n8
  %returnValue = call i32 @baka_add(ptr %res5, i32 %i6, i32 %mod)
  store i32 %returnValue, ptr %res, align 4
  %i9 = load i32, ptr %i, align 4
  %add10 = add i32 %i9, 1
  store i32 %add10, ptr %i, align 4
  br label %whileCond

whileEnd:                                         ; preds = %whileCond
  store i32 0, ptr %i, align 4
  %sum = alloca i32, align 4
  store i32 0, ptr %sum, align 4
  br label %whileCond11

whileCond11:                                      ; preds = %whileBody12, %whileEnd
  %i14 = load i32, ptr %i, align 4
  %n15 = load i32, ptr %n, align 4
  %lt16 = icmp slt i32 %i14, %n15
  %zext_res17 = zext i1 %lt16 to i32
  %icmp18 = icmp ne i32 %zext_res17, 0
  br i1 %icmp18, label %whileBody12, label %whileEnd13

whileBody12:                                      ; preds = %whileCond11
  %sum19 = load i32, ptr %sum, align 4
  %i20 = load i32, ptr %i, align 4
  %a21 = load ptr, ptr %a, align 8
  %res22 = getelementptr i32, ptr %a21, i32 0, i32 %i20
  %"a[i]" = load i32, ptr %res22, align 4
  %add23 = add i32 %sum19, %"a[i]"
  store i32 %add23, ptr %sum, align 4
  %i24 = load i32, ptr %i, align 4
  %add25 = add i32 %i24, 1
  store i32 %add25, ptr %i, align 4
  br label %whileCond11

whileEnd13:                                       ; preds = %whileCond11
  %sum26 = load i32, ptr %sum, align 4
  ret i32 %sum26
  ret i32 0
}

define i32 @main() {
mainEntry:
  %cirno = alloca <9 x i32>, align 64
  %pointer = getelementptr <9 x i32>, ptr %cirno, i32 0, i32 0
  store i32 1, ptr %pointer, align 4
  %pointer1 = getelementptr <9 x i32>, ptr %cirno, i32 0, i32 1
  store i32 3, ptr %pointer1, align 4
  %pointer2 = getelementptr <9 x i32>, ptr %cirno, i32 0, i32 2
  store i32 5, ptr %pointer2, align 4
  %pointer3 = getelementptr <9 x i32>, ptr %cirno, i32 0, i32 3
  store i32 7, ptr %pointer3, align 4
  %pointer4 = getelementptr <9 x i32>, ptr %cirno, i32 0, i32 4
  store i32 9, ptr %pointer4, align 4
  %pointer5 = getelementptr <9 x i32>, ptr %cirno, i32 0, i32 5
  store i32 2, ptr %pointer5, align 4
  %pointer6 = getelementptr <9 x i32>, ptr %cirno, i32 0, i32 6
  store i32 4, ptr %pointer6, align 4
  %pointer7 = getelementptr <9 x i32>, ptr %cirno, i32 0, i32 7
  store i32 6, ptr %pointer7, align 4
  %pointer8 = getelementptr <9 x i32>, ptr %cirno, i32 0, i32 8
  store i32 8, ptr %pointer8, align 4
  %res = getelementptr <9 x i32>, ptr %cirno, i32 0, i32 0
  %returnValue = call i32 @kyu(ptr %res, i32 9)
  ret i32 %returnValue
  ret i32 0
}
