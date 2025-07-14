import gradio as gr
import requests
import tempfile
import os
import uuid
import re
from urllib.parse import unquote

# API端点URL，需替换为实际接口地址
API_URL = "http://127.0.0.1:6024/XXX/XXX"
EXCEL_API_URL = "http://127.0.0.1:6024/XXX/excel"

def convert_text_to_document(text):
    """将文本发送到API并返回生成的报告路径"""
    if not text.strip():
        return None, "请输入文本内容"
    
    try:
        # 发送POST请求到API
        response = requests.post(
            API_URL,
            data=text.encode('utf-8'),
            headers={'Content-Type': 'application/json'}
        )
        
        # 检查响应状态
        if response.status_code == 200:
            # 尝试从响应头中获取文件名
            filename = None
            content_disposition = response.headers.get('Content-Disposition')
            if content_disposition:
                # 从Content-Disposition头中提取文件名
                for part in content_disposition.split(';'):
                    part = part.strip()
                    if part.startswith('filename='):
                        filename = part.split('=')[1].strip('"')
                        filename = unquote(filename, 'utf-8')
                        break
            
            # 如果没有找到文件名，生成一个唯一的文件名
            if not filename:
                filename = f"document_{uuid.uuid4().hex[:8]}.docx"
            
            # 创建临时文件存储响应内容，使用获取到的文件名
            temp_dir = tempfile.gettempdir()
            file_path = os.path.join(temp_dir, filename)
            
            with open(file_path, 'wb') as f:
                f.write(response.content)
            
            return file_path, "报告生成成功，请点击下载链接获取"
        else:
            return None, f"API请求失败，状态码：{response.status_code}"
    
    except Exception as e:
        return None, f"发生错误：{str(e)}"

def convert_excel_to_zip(file):
    """将Excel文件发送到API并返回生成的压缩包路径"""
    if not file:
        return None, "请上传Excel文件"
    
    try:
        # 发送POST请求到API，携带文件
        with open(file.name, 'rb') as f:
            response = requests.post(
                EXCEL_API_URL,
                files={'file': f}
            )
        
        # 检查响应状态
        if response.status_code == 200:
            # 尝试从响应头中获取文件名
            filename = None
            content_disposition = response.headers.get('Content-Disposition')
            if content_disposition:
                # 从Content-Disposition头中提取文件名
                for part in content_disposition.split(';'):
                    part = part.strip()
                    if part.startswith('filename='):
                        filename = part.split('=')[1].strip('"')
                        filename = unquote(filename, 'utf-8')
                        break
            
            # 如果没有找到文件名，生成一个唯一的文件名
            if not filename:
                filename = f"documents_{uuid.uuid4().hex[:8]}.zip"
            
            # 创建临时文件存储响应内容，使用获取到的文件名
            temp_dir = tempfile.gettempdir()
            file_path = os.path.join(temp_dir, filename)
            
            with open(file_path, 'wb') as f:
                f.write(response.content)
            
            return file_path, "报告生成成功，请点击下载链接获取"
        else:
            return None, f"API请求失败，状态码：{response.status_code}"
    
    except Exception as e:
        return None, f"发生错误：{str(e)}"

def clear_text():
    """清除文本输入框内容"""
    return ""

# 创建Gradio界面
with gr.Blocks(title="结肠癌结构化报告生成工具", theme=gr.themes.Soft()) as demo:
    gr.Markdown("# 结肠癌结构化报告生成工具")
    
    with gr.Row():
        with gr.Column(scale=1):
            # 创建选项卡，包含文本输入和Excel上传两种模式
            with gr.Tabs() as input_tabs:
                with gr.Tab("文本输入"):
                    with gr.Row():
                        text_input = gr.Textbox(
                            label="输入非结构化报告内容",
                            placeholder="请在此输入需要转换非结构化报告的文本...",
                            lines=20,
                            max_lines=30
                        )
                    
                    with gr.Row():
                        convert_text_btn = gr.Button("生成结肠癌结构化报告", variant="primary")
                        clear_text_btn = gr.Button("清除文本", variant="secondary")
                
                with gr.Tab("Excel上传"):
                    excel_input = gr.File(
                        label="上传Excel文件",
                        file_types=[".xlsx", ".xls"],
                        file_count="single"
                    )
                    gr.Markdown("**注意：** excel中的数据必须逐行连续，不可中断")
                    convert_excel_btn = gr.Button("批量生成结肠癌结构化报告", variant="primary")
        
        with gr.Column(scale=1):
            file_output = gr.File(label="结构化结肠癌报告")
            status_message = gr.Textbox(label="状态", interactive=False)
    
    # 设置按钮点击事件
    convert_text_btn.click(
        fn=convert_text_to_document,
        inputs=[text_input],
        outputs=[file_output, status_message]
    )
    
    convert_excel_btn.click(
        fn=convert_excel_to_zip,
        inputs=[excel_input],
        outputs=[file_output, status_message]
    )
    
    # 清除文本按钮事件
    clear_text_btn.click(
        fn=clear_text,
        inputs=[],
        outputs=[text_input]
    )
    
    # 添加示例文本
    with gr.Row():
        examples = gr.Examples(
            examples=[
                ["1	男	67岁	CT	胸部,上腹部,盆腔	平扫,平扫.增强,平扫.增强	2021-12-30	17:23:07	  两侧胸廓对称，两肺纹理稍增多，两肺未见明显异常密度影。气管居中，气管支气管通畅，纵隔内未见明显肿大淋巴结影。冠脉管壁局部钙化。左侧胸腔极少量积液。PICC管置入后，头端为上腔静脉内。   肝脏大小、形态正常，肝内见无强化小囊性灶。肝内外胆管未见明显扩张。胆囊形态如常，腔内未见明显异常密度影。脾脏不大，内见囊性灶，未见强化。胰腺形态、大小及密度未见明显异常。双肾形态正常，双肾见无强化囊性灶。双侧肾窦内见斑点状致密影。左侧肾上腺增粗。腹膜后未见明显肿大淋巴结影。   直肠MT术后，吻合口壁稍增厚；周围脂肪间隙模糊，乙状结肠局部肠壁增厚。膀胱充盈一般，壁光整，右后壁局部见点状致密影。前列腺及双侧精囊腺未见异常。盆腔内见少量积液。	直肠MT术后改变，吻合口壁稍增厚伴周围脂肪间隙模糊，乙状结肠局部肠壁增厚；建议结合肠镜检查。 左侧胸腔极少量积液；冠脉局部钙化。 肝囊肿；双肾囊肿；双肾结石；脾脏小囊性灶。 左侧肾上腺增粗；膀胱小结石。 盆腔少量积液。 请结合临床。	3302813269		0001092526"],
                ["2	女	68岁	CT	胸部,上腹部,中腹部,盆腔	平扫.增强,平扫.增强,平扫.增强,平扫.增强	2021-12-29	09:17:21	　　左肺术后复查：术区见少许索条影及软组织影，大小约1.9×1.6cm，增强后轻度强化；余两侧胸廓对称，两肺纹理增多、紊乱，两肺散在少许条索影及小结节灶。双肺门影无增大，各叶段以上支气管开口通畅。心影及大血管形态正常，纵隔内见小淋巴结。 　　肝脏大小、形态正常，肝内多发低密度影，右后叶为著，大小约4.1×3.2cm，边界欠清，增强后环状强化；另肝内见小圆形囊性无强化灶。胆囊不大，底壁增厚，未见异常密度影。胰腺大小、形态及密度未见明显异常。脾不大。左肾见多发无强化类圆形囊性无强化灶，右肾形态、大小未见明显异常。左侧肾上腺稍增粗，均匀性强化；腹腔内及腹膜后未见肿大淋巴结。 　　膀胱充盈尚可，膀胱壁未见明显增厚，内未见异常密度影。子宫形态大小未见异常，双侧附件区未见占位灶。盆腔内未见肿大淋巴结。乙状结肠及直肠肠壁见迂曲高密度影。	左肺MT术后病例，术区软组织影，考虑复发可能大；肝内多发病灶，考虑M，较前9-10进展；请结合临床； 两肺结节灶及索条影，随访。 肝、左肾囊肿，胆囊底壁增厚；左侧肾上腺稍增粗； 乙状结肠及直肠肠壁钙化灶，血吸虫陈旧感染所致？请结合临床病史。	3302546801		0001091617"],
                ["3	女	66岁	CT	胸部,上腹部,盆腔	平扫,平扫.增强,平扫.增强	2021-12-29	10:41:57	两肺野清晰，右肺中叶(Im174)小类结节，长径约3mm，，两肺少许条索影，余肺内未见明显异常密度影。气管及主要支气管通畅。纵隔、肺门未见肿大淋巴结影。胸腔内未见积液征。 胃充盈尚可，胃窦部胃壁稍增厚，增强后见稍强化改变；肝脏外形、大小、各叶比例如常，肝内见多发囊性无强化低密度灶，余肝内密度均匀，未见明显异常强化灶；双侧肾上腺未见明显增粗；胆囊未见；胰、脾、双肾大小、形态正常，未见异常密度影及强化灶。直肠及左侧结肠见致密影，其周围肠壁未见明显异常增厚及强化改变，余肠曲分布、形态及密度未见异常，腹腔及腹膜后未见软组织肿块。膀胱充盈良好，膀胱壁未见明显异常增厚及强化改变；子宫及双侧附件显示可。余未见特殊。	右肺中叶小类结节，随诊复查；两肺少许条索影； 胃窦部情况建议结合胃镜检查； 肝囊肿； 直肠及左侧结肠术后改变。	3302813219		0001092805"]
            ],
            inputs=[text_input]
        )
    


# 启动应用
if __name__ == "__main__":
    demo.launch(server_name="0.0.0.0", server_port=6024)    