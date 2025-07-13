import gradio as gr
import requests
import tempfile
import os
import uuid
import re
from urllib.parse import unquote

# API端点URL，需替换为实际接口地址
API_URL = "http://115.120.241.249:6023/api/report/structuredDocument"

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

# 创建Gradio界面
with gr.Blocks(title="结肠癌结构化报告生成工具", theme=gr.themes.Soft()) as demo:
    gr.Markdown("# 结肠癌结构化报告生成工具")
    
    with gr.Row():
        with gr.Column(scale=1):
            text_input = gr.Textbox(
                label="输入非结构化报告内容",
                placeholder="请在此输入需要转换非结构化报告的文本...",
                lines=20,
                max_lines=30
            )
            convert_btn = gr.Button("生成结肠癌结构化报告", variant="primary")
        
        with gr.Column(scale=1):
            file_output = gr.File(label="结构化结肠癌报告")
            status_message = gr.Textbox(label="状态", interactive=False)
    
    # 设置按钮点击事件
    convert_btn.click(
        fn=convert_text_to_document,
        inputs=[text_input],
        outputs=[file_output, status_message]
    )
    
    # 添加示例文本
    gr.Examples(
        examples=[
            ["序号	性别	年龄	检查类型	检查部位	检查方法	检查日期	检查时间	放射学表现	放射学表现	影像号	门诊号	住院号\n1	男	67岁	CT	胸部,上腹部,盆腔	平扫,平扫.增强,平扫.增强	2021-12-30	17:23:07	  两侧胸廓对称，两肺纹理稍增多，两肺未见明显异常密度影。气管居中，气管支气管通畅，纵隔内未见明显肿大淋巴结影。冠脉管壁局部钙化。左侧胸腔极少量积液。PICC管置入后，头端为上腔静脉内。   肝脏大小、形态正常，肝内见无强化小囊性灶。肝内外胆管未见明显扩张。胆囊形态如常，腔内未见明显异常密度影。脾脏不大，内见囊性灶，未见强化。胰腺形态、大小及密度未见明显异常。双肾形态正常，双肾见无强化囊性灶。双侧肾窦内见斑点状致密影。左侧肾上腺增粗。腹膜后未见明显肿大淋巴结影。   直肠MT术后，吻合口壁稍增厚；周围脂肪间隙模糊，乙状结肠局部肠壁增厚。膀胱充盈一般，壁光整，右后壁局部见点状致密影。前列腺及双侧精囊腺未见异常。盆腔内见少量积液。	直肠MT术后改变，吻合口壁稍增厚伴周围脂肪间隙模糊，乙状结肠局部肠壁增厚；建议结合肠镜检查。 左侧胸腔极少量积液；冠脉局部钙化。 肝囊肿；双肾囊肿；双肾结石；脾脏小囊性灶。 左侧肾上腺增粗；膀胱小结石。 盆腔少量积液。 请结合临床。	3302813269		0001092526"]
        ],
        inputs=[text_input]
    )

# 启动应用
if __name__ == "__main__":
    demo.launch(    server_name="0.0.0.0",
    server_port=6024)