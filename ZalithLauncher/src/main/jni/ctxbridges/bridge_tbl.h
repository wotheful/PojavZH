//
// Created by maks on 18.10.2023.
//

#ifndef __POJAVLAUNCHER_BRIDGE_TBL_H_
#define __POJAVLAUNCHER_BRIDGE_TBL_H_

#include <ctxbridges/common.h>
#include <ctxbridges/gl_bridge.h>
#include <ctxbridges/osm_bridge.h>

typedef basic_render_window_t* (*br_init_context_t)(basic_render_window_t* share);
typedef void (*br_make_current_t)(basic_render_window_t* bundle);
typedef basic_render_window_t* (*br_get_current_t)(void);

static void set_osm_bridge_tbl(void);
static void set_gl_bridge_tbl(void);

bool (*br_init)(void) = NULL;
br_init_context_t br_init_context = NULL;
br_make_current_t br_make_current = NULL;
br_get_current_t br_get_current = NULL;
void (*br_swap_buffers)(void) = NULL;
void (*br_setup_window)(void) = NULL;
void (*br_swap_interval)(int swapInterval) = NULL;


void set_osm_bridge_tbl(void) {
    br_init = osm_init;
    br_init_context = (br_init_context_t) osm_init_context;
    br_make_current = (br_make_current_t) osm_make_current;
    br_get_current = (br_get_current_t) osm_get_current;
    br_swap_buffers = osm_swap_buffers;
    br_setup_window = osm_setup_window;
    br_swap_interval = osm_swap_interval;
}

void set_gl_bridge_tbl(void) {
    br_init = gl_init;
    br_init_context = (br_init_context_t) gl_init_context;
    br_make_current = (br_make_current_t) gl_make_current;
    br_get_current = (br_get_current_t) gl_get_current;
    br_swap_buffers = gl_swap_buffers;
    br_setup_window = gl_setup_window;
    br_swap_interval = gl_swap_interval;
}

#endif //__POJAVLAUNCHER_BRIDGE_TBL_H_
