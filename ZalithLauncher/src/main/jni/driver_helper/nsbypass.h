
//
// Created by maks on 05.06.2023.
//

#ifndef __POJAVLAUNCHER_NSBYPASS_H_
#define __POJAVLAUNCHER_NSBYPASS_H_

#include <stdbool.h>

bool linker_ns_load(const char* lib_search_path);
void* linker_ns_dlopen(const char* name, int flag);
void* linker_ns_dlopen_unique(const char* tmpdir, const char* name, int flag);

#endif //__POJAVLAUNCHER_NSBYPASS_H_
