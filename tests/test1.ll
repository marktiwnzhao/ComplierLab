; ModuleID = 'module'
source_filename = "module"

@g_var = global i32 2

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 1, ptr %a, align 4
  %b = alloca i32, align 4
  %b1 = load i32, ptr %b, align 4
  store i32 %b1, ptr %a, align 4
  %a2 = load i32, ptr %a, align 4
  %g_var = load i32, ptr @g_var, align 4
  %add = add i32 %a2, %g_var
  ret i32 %add
}
