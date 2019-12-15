package com.bresai.expecto.patronum.core.bean.result;

import com.bresai.expecto.patronum.core.enums.FileDiffEnum;
import lombok.Data;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/15
 * @content:
 */
@Data
public class FileDiff {
    private FileDiffEnum diffEnum;

    /**
     * the filepath, if the diffEnum is rename,
     * then filepath indicate the old filepath before rename
     */
    private String filepath;

    /**
     * if the diffEnum is rename,
     * then filepath indicate the old filepath before rename
     * otherwise it's null;
     */
    private String newFilepath;
}
