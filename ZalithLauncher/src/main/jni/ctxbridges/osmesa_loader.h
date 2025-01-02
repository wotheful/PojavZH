//
// Created by maks on 21.09.2022.
//

#ifndef __POJAVLAUNCHER_OSMESA_LOADER_H_
#define __POJAVLAUNCHER_OSMESA_LOADER_H_

#include <GL/osmesa.h>
#include <stdbool.h>

extern GLboolean (*OSMesaMakeCurrent_p) (OSMesaContext ctx, void *buffer, GLenum type,
                                         GLsizei width, GLsizei height);
extern OSMesaContext (*OSMesaGetCurrentContext_p) (void);
extern OSMesaContext  (*OSMesaCreateContext_p) (GLenum format, OSMesaContext sharelist);
extern void (*OSMesaDestroyContext_p) (OSMesaContext ctx);
extern void (*OSMesaPixelStore_p) ( GLint pname, GLint value );
extern GLubyte* (*glGetString_p) (GLenum name);
extern void (*glFinish_p) (void);
extern void (*glClearColor_p) (GLclampf red, GLclampf green, GLclampf blue, GLclampf alpha);
extern void (*glClear_p) (GLbitfield mask);
extern void (*glReadPixels_p) (GLint x, GLint y, GLsizei width, GLsizei height, GLenum format, GLenum type, void * data);
extern void* (*OSMesaGetProcAddress_p)(const char* funcName);

bool dlsym_OSMesa(void);
#endif //__POJAVLAUNCHER_OSMESA_LOADER_H_
