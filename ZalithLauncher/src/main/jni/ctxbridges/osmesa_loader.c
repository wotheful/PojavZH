//
// Modifiled by Vera-Firefly on 19.09.2024.
//
#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include "environ/environ.h"
#include "osmesa_loader.h"
#include "renderer_config.h"

GLboolean (*OSMesaMakeCurrent_p) (OSMesaContext ctx, void *buffer, GLenum type,
                                         GLsizei width, GLsizei height);
OSMesaContext (*OSMesaGetCurrentContext_p) (void);
OSMesaContext  (*OSMesaCreateContext_p) (GLenum format, OSMesaContext sharelist);
void (*OSMesaDestroyContext_p) (OSMesaContext ctx);
void (*OSMesaPixelStore_p) ( GLint pname, GLint value );
GLubyte* (*glGetString_p) (GLenum name);
void (*glFinish_p) (void);
void (*glClearColor_p) (GLclampf red, GLclampf green, GLclampf blue, GLclampf alpha);
void (*glClear_p) (GLbitfield mask);
void (*glReadPixels_p) (GLint x, GLint y, GLsizei width, GLsizei height, GLenum format, GLenum type, void * data);
void* (*OSMesaGetProcAddress_p)(const char* funcName);

bool dlsym_OSMesa(void) {
    char* mesa_library = getenv("MESA_LIBRARY");
    void* dl_handle = dlopen(mesa_library, RTLD_LOCAL | RTLD_LAZY);
    if(dl_handle == NULL) return false;
    OSMesaGetProcAddress_p = dlsym(dl_handle, "OSMesaGetProcAddress");

    if(OSMesaGetProcAddress_p == NULL) {
        printf("%s\n", dlerror());
        return false;
    }
    OSMesaMakeCurrent_p = dlsym(dl_handle, "OSMesaMakeCurrent");
    OSMesaGetCurrentContext_p = dlsym(dl_handle,"OSMesaGetCurrentContext");
    OSMesaCreateContext_p = dlsym(dl_handle, "OSMesaCreateContext");
    OSMesaDestroyContext_p = dlsym(dl_handle, "OSMesaDestroyContext");
    OSMesaPixelStore_p = dlsym(dl_handle,"OSMesaPixelStore");
    glGetString_p = dlsym(dl_handle,"glGetString");
    glClearColor_p = dlsym(dl_handle, "glClearColor");
    glClear_p = dlsym(dl_handle,"glClear");
    glFinish_p = dlsym(dl_handle,"glFinish");
    glReadPixels_p = dlsym(dl_handle,"glReadPixels");
    return true;
}
