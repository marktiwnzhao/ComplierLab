; ModuleID = 'module'
source_filename = "module"

define void @b() {
bEntry:
  ret void
}

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 1, ptr %a, align 4
  call void @b()
  %a1 = load i32, ptr %a, align 4
  ret i32 %a1
}
