package com.bresai.expecto.patronum.core.file;

import com.bresai.expecto.patronum.core.StdOutLoggerBridge;
import com.bresai.expecto.patronum.core.enums.FileDiffEnum;
import com.bresai.expecto.patronum.core.bean.result.FileDiff;
import com.bresai.expecto.patronum.core.walker.JavaWalker;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
public class testJavaWalker {
    String gitDiff = "R100\tfileToRenameOld.java\tfileToRenameNew.java\nA\tfileToAdd.java\nD\tfileToDelete.java\nM\tfileToModify.java";

    @Test
    public void testFileDiff(){
        JavaWalker javaWalker = new JavaWalker(new StdOutLoggerBridge(false), FileUtils.createTempFile(",","",new File("")));
        Map<FileDiffEnum, List<FileDiff>> map = javaWalker.searchGitFileDiff(gitDiff);

        assertEquals(map.get(FileDiffEnum.ADD).get(0).getFilepath(), "fileToAdd.java");
        assertEquals(map.get(FileDiffEnum.DELETE).get(0).getFilepath(), "fileToDelete.java");
        assertEquals(map.get(FileDiffEnum.MODIFY).get(0).getFilepath(), "fileToModify.java");
        assertEquals(map.get(FileDiffEnum.RENAME).get(0).getFilepath(), "fileToRenameOld.java");
        assertEquals(map.get(FileDiffEnum.RENAME).get(0).getNewFilepath(), "fileToRenameNew.java");
    }

}
