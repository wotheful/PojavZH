//
// Created by Vera-Firefly on 20.08.2024.
//

#ifndef __VIRGL_BRIDGE_H_
#define __VIRGL_BRIDGE_H_

void* virglGetCurrentContext();
void loadSymbolsVirGL();
int virglInit();
void virglSwapBuffers();
void virglMakeCurrent(void* window);
void* virglCreateContext(void* contextSrc);
void virglSwapInterval(int interval);

#endif //__VIRGL_BRIDGE_H_
