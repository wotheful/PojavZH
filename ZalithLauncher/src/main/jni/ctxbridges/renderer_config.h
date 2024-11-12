//
// Created by Vera-Firefly on 2.12.2023.
// Definitions specific to the renderer
//

#ifndef __POTATOBRIDGE_H
#define __POTATOBRIDGE_H

#include <EGL/egl.h>

#define RENDERER_GL4ES 1
#define RENDERER_VK_ZINK 2
#define RENDERER_VIRGL 3
#define RENDERER_VULKAN 4


struct PotatoBridge {
    void* eglContext;    // EGLContext
    void* eglDisplay;    // EGLDisplay
    void* eglSurface;    // EGLSurface
    // void* eglSurfaceRead;
    // void* eglSurfaceDraw;
};

extern struct PotatoBridge potatoBridge;
extern EGLConfig config;

#endif // __POTATOBRIDGE_H


