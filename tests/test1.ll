; ModuleID = 'module'
source_filename = "module"

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 0, ptr %a, align 4
  %count = alloca i32, align 4
  store i32 0, ptr %count, align 4
  br label %whileCond

whileCond:                                        ; preds = %next, %mainEntry
  %a1 = load i32, ptr %a, align 4
  %le = icmp sle i32 %a1, 0
  %zext_res = zext i1 %le to i32
  %icmp = icmp ne i32 %zext_res, 0
  br i1 %icmp, label %whileBody, label %whileEnd

whileBody:                                        ; preds = %whileCond
  %a2 = load i32, ptr %a, align 4
  %sub = sub i32 %a2, 1
  store i32 %sub, ptr %a, align 4
  %count3 = load i32, ptr %count, align 4
  %add = add i32 %count3, 1
  store i32 %add, ptr %count, align 4
  %a4 = load i32, ptr %a, align 4
  %lt = icmp slt i32 %a4, -20
  %zext_res5 = zext i1 %lt to i32
  %icmp6 = icmp ne i32 %zext_res5, 0
  br i1 %icmp6, label %ifTrue, label %ifFalse

whileEnd:                                         ; preds = %ifTrue, %whileCond
  %count7 = load i32, ptr %count, align 4
  ret i32 %count7

ifTrue:                                           ; preds = %whileBody
  br label %whileEnd
  br label %next

ifFalse:                                          ; preds = %whileBody
  br label %next

next:                                             ; preds = %ifFalse, %ifTrue
  br label %whileCond
}
