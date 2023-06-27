; ModuleID = 'module'
source_filename = "module"

define i32 @main() {
mainEntry:
  %a = alloca <2 x i32>, align 8
  %pointer = getelementptr <2 x i32>, ptr %a, i32 0, i32 0
  store i32 0, ptr %pointer, align 4
  %pointer1 = getelementptr <2 x i32>, ptr %a, i32 0, i32 1
  store i32 0, ptr %pointer1, align 4
  %b = alloca i32, align 4
  %res = getelementptr <2 x i32>, ptr %a, i32 0, i32 1
  %"a[1]" = load i32, ptr %res, align 4
  store i32 %"a[1]", ptr %b, align 4
  ret i32 0
}
