/* SPDX-License-Identifier: GPL-3.0-or-later */

package com.movtery.zalithlauncher.utils.file;

import java.io.File;
import java.io.IOException;

/** Resolves untrusted paths without allowing traversal or escaping through symlinks. */
public final class SafePaths {
    private SafePaths() {}

    public static boolean isWithin(File root, File target) throws IOException {
        return target.getCanonicalFile().toPath().startsWith(root.getCanonicalFile().toPath());
    }

    public static File resolveChild(File root, String relativePath) throws IOException {
        if (relativePath == null || new File(relativePath).isAbsolute()
                || relativePath.indexOf('\\') >= 0 || relativePath.matches("^[a-zA-Z]:.*")) {
            throw new SecurityException("Invalid relative path");
        }
        for (String part : relativePath.split("/")) {
            if (part.equals("..")) {
                throw new SecurityException("Parent traversal is not allowed");
            }
        }
        File target = new File(root, relativePath);
        if (!isWithin(root, target)) {
            throw new SecurityException("Path escapes its allowed directory");
        }
        return target;
    }

    public static void validateRelativeSymlink(File root, File link, String target) throws IOException {
        if (target == null || target.isEmpty() || new File(target).isAbsolute()
                || target.indexOf('\\') >= 0 || target.matches("^[a-zA-Z]:.*")
                || !isWithin(root, link)
                || !isWithin(root, new File(link.getParentFile(), target))) {
            throw new SecurityException("Symlink escapes its allowed directory");
        }
    }
}
