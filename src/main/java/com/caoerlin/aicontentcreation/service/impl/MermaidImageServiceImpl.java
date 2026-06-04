package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.config.MermaidConfig;
import com.caoerlin.aicontentcreation.constant.ImageConstont;
import com.caoerlin.aicontentcreation.model.dto.image.ImageData;
import com.caoerlin.aicontentcreation.model.dto.image.ImageRequest;
import com.caoerlin.aicontentcreation.model.enums.ImageMethodEnum;
import com.caoerlin.aicontentcreation.service.ImageSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class MermaidImageServiceImpl implements ImageSearchService {
    private final MermaidConfig mermaidConfig;

    @Override
    public String searchImage(String keywords) {
        return "";
    }

    @Override
    public String getImage(ImageRequest request) {
        return ImageSearchService.super.getImage(request);
    }

    @Override
    public ImageData getImageDate(ImageRequest request) {
        String mermaidCode = request.getEffectiveParam(true);
        return generateDiagramData(mermaidCode);
    }

    private ImageData generateDiagramData(String mermaidCode) {
        if (StrUtil.isBlank(mermaidCode)) {
            log.warn("mermaid 生产代码为空");
            return null;
        }

        File tempOutputFile = null;
        File tempInputFile = null;

        try {
            //生产输入文件
            tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
            FileUtil.writeUtf8String(mermaidCode, tempInputFile);

            //创建临时输出文件
            String outputFileSuffix = "." + mermaidConfig.getOutputFormat();
            tempOutputFile = FileUtil.createTempFile("mermaid_output_", outputFileSuffix, true);

            //转换为图片
            convertMermaidToImage(tempInputFile, tempOutputFile);

            //读取图片字节信息
            byte[] imageBytes = FileUtil.readBytes(tempOutputFile);
            String mimeType = getMimeType(mermaidConfig.getOutputFormat());

            log.info("Mermaid 图表生成成功,size={} bytes", imageBytes.length);
            return ImageData.fromBytes(imageBytes, mimeType);
        } catch (Exception e) {
            log.error("Mermaid 图表生成失败,e={}", e.getMessage());
            return null;
        } finally {
            //清理文件
            if (FileUtil.isEmpty(tempInputFile)) {
                FileUtil.del(tempInputFile);
            }

            if (FileUtil.isEmpty(tempOutputFile)) {
                FileUtil.del(tempOutputFile);
            }
        }
    }

    /**
     * 获取 MimeType
     *
     * @param format 文件格式
     * @return MimeType
     */
    private String getMimeType(String format) {
        return switch (format) {
            case "svg" -> "image/svg+xml";
            case "png" -> "image/png";
            case "pdf" -> "appliction.pdf";
            default -> "image/png";
        };
    }

    /**
     * 生成Mermaid，并转换为图片文件
     *
     * @param inputFile mermaid 文件
     * @param outputFile 图片文件
     */
    private void convertMermaidToImage(File inputFile, File outputFile) {
        String cliCommand = mermaidConfig.getCliCommandForOs();

        try {
            //创建执行语句
            // mmdc -i diagram.mmd -o output.png -b transparent
            String command = String.format("%s -i %s -o %s -b %s",
                    cliCommand,
                    inputFile.getAbsolutePath(),
                    outputFile.getAbsolutePath(),
                    mermaidConfig.getBackgroundColor()
            );

            //如果设置了宽度添加宽度
            //mmdc -i diagram.mmd -o output.png -b transparent -w 1200
            if (ObjectUtil.isNotNull(mermaidConfig.getWidth())) {
                command += "-w" + mermaidConfig.getWidth();
            }

            log.info("开始执行 Mermaid Cli 命令,command={}", command);

            //执行命令
            String result = RuntimeUtil.execForStr(command);

            log.info("Mermaid 命令执行成功,result={}", result);
        } catch (IORuntimeException e) {
            log.error("Mermaid Cli 命令执行失败,e={}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid Cli 命令执行失败");
        }

    }

    @Override
    public boolean isAvailable() {
        return ImageSearchService.super.isAvailable();
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.MERMAID;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(ImageConstont.PICSUM_URL_TEMPLATE, position);
    }
}
