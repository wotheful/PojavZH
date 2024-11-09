//
// Created by maks on 18.10.2023.
//

#include <ctxbridges/common.h>
#include <ctxbridges/gl_bridge.h>
#include <ctxbridges/osm_bridge.h>

typedef basic_render_window_t* (*br_init_context_t)(basic_render_window_t* share);
typedef void (*br_make_current_t)(basic_render_window_t* bundle);
typedef basic_render_window_t* (*br_get_current_t)();

static bool (*br_init)() = NULL;
static br_init_context_t br_init_context = NULL;
static br_make_current_t br_make_current = NULL;
static br_get_current_t br_get_current = NULL;
