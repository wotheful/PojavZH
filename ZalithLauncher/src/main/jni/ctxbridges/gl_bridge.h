//
// Created by maks on 17.09.2022.
//
#include <EGL/egl.h>
#include <stdbool.h>
#ifndef POJAVLAUNCHER_GL_BRIDGE_H
#define POJAVLAUNCHER_GL_BRIDGE_H

typedef struct {
    char       state;
    struct ANativeWindow *nativeSurface;
    struct ANativeWindow *newNativeSurface;
    EGLConfig  config;
    EGLint     format;
    EGLContext context;
    EGLSurface surface;
} gl_render_window_t;

bool gl_init();
gl_render_window_t* gl_get_current();
gl_render_window_t* gl_init_context(gl_render_window_t* share);
static void gl_make_current(gl_render_window_t* bundle);
static void gl_swap_buffers();
static void gl_setup_window();
static void gl_swap_interval(int swapInterval);


#endif //POJAVLAUNCHER_GL_BRIDGE_H
