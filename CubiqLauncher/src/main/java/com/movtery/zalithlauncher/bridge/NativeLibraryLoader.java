/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.bridge;

public class NativeLibraryLoader {
    public static void loadPojavLib() {
        try {
            System.loadLibrary("pojavexec");
        } catch (UnsatisfiedLinkError e) {
            String nativeDir = System.getenv("POJAV_NATIVEDIR");
            if (nativeDir != null && new java.io.File(nativeDir, "libpojavexec.so").exists()) {
                System.load(nativeDir + "/libpojavexec.so");
            } else {
                throw e;
            }
        }
    }

    public static void loadExitHookLib() {
        try {
            System.loadLibrary("exithook");
        } catch (UnsatisfiedLinkError e) {
            String nativeDir = System.getenv("POJAV_NATIVEDIR");
            if (nativeDir != null && new java.io.File(nativeDir, "libexithook.so").exists()) {
                System.load(nativeDir + "/libexithook.so");
            } else {
                throw e;
            }
        }
    }

    public static void loadPojavAWTLib() {
        try {
            System.loadLibrary("pojavexec_awt");
        } catch (UnsatisfiedLinkError e) {
            String nativeDir = System.getenv("POJAV_NATIVEDIR");
            if (nativeDir != null && new java.io.File(nativeDir, "libpojavexec_awt.so").exists()) {
                System.load(nativeDir + "/libpojavexec_awt.so");
            } else {
                throw e;
            }
        }
    }
}
