//
// Created by Vera-Firefly on 20.08.2024.
//

#ifndef __VIRGL_BRIDGE_H_
#define __VIRGL_BRIDGE_H_

bool loadSymbolsVirGL(void);
int virglInit(void);
void* virglCreateContext(void* contextSrc);
void* virglGetCurrentContext(void);
void virglMakeCurrent(void* window);
void virglSwapBuffers(void);
void virglSwapInterval(int interval);

#endif //__VIRGL_BRIDGE_H_
