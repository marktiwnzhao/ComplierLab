; ModuleID = 'module'
source_filename = "module"

define i32 @main(i32 %0) {
mainEntry:
  %num = alloca i32, align 4
  store i32 %0, ptr %num, align 4
  %num1 = load i32, ptr %num, align 4
  %lt = icmp slt i32 %num1, 9
  %zext_res = zext i1 %lt to i32
  %icmp = icmp ne i32 %zext_res, 0
  br i1 %icmp, label %ifTrue, label %orFalse

ifTrue:                                           ; preds = %orFalse, %mainEntry
  store i32 1, ptr %num, align 4
  br label %next

ifFalse:                                          ; preds = %orFalse
  store i32 0, ptr %num, align 4
  br label %next

next:                                             ; preds = %ifFalse, %ifTrue
  %num5 = load i32, ptr %num, align 4
  ret i32 %num5

orFalse:                                          ; preds = %mainEntry
  %num2 = load i32, ptr %num, align 4
  %gt = icmp sgt i32 %num2, 2
  %zext_res3 = zext i1 %gt to i32
  %icmp4 = icmp ne i32 %zext_res3, 0
  br i1 %icmp4, label %ifTrue, label %ifFalse
}
