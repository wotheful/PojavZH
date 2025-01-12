//
// Created by Vera-Firefly on 20.08.2024.
//

#ifndef __VIRGL_BRIDGE_H_
#define __VIRGL_BRIDGE_H_

bool loadSymbolsVirGL();
int virglInit();
void* virglCreateContext(void* contextSrc);
void* virglGetCurrentContext();
void virglMakeCurrent(void* window);
void virglSwapBuffers();
void virglSwapInterval(int interval);

#endif //__VIRGL_BRIDGE_H_
