# viodemo

## 1) Descrição

O **Vio** é um sistema de certificação de autenticidade de documentos de identificação. Os documentos compatíveis possuem o **QR Code Vio**, um QR Code que replica os dados contidos no documento utilizando **encriptação forte** para garantir a autenticidade. Além disso, o QR Code Vio é encriptado na fonte com informações fornecidas pela própria **entidade emissora** do documento, garantindo o **não-repúdio**.

Para mais informações sobre a documentação, consulte:  
[API Vio Decode Documentation](https://apicenter.estaleiro.serpro.gov.br/documentacao/vio-decode/pt/)

## 2) Endpoints

### Api Vio Encoder

- **Produção**:  
  [https://gateway.apiserpro.serpro.gov.br/vioenc/v1/encode](https://gateway.apiserpro.serpro.gov.br/vioenc/v1/encode)

- **Homologação**:  
  [https://gateway.apiserpro.serpro.gov.br/vio-encoder-default/v1/encode](https://gateway.apiserpro.serpro.gov.br/vio-encoder-default/v1/encode)

### Api Vio Decoder

- **Produção**:  
  [https://gateway.apiserpro.serpro.gov.br/viodec](https://gateway.apiserpro.serpro.gov.br/viodec)

- **Homologação**:  
  [https://gateway.apiserpro.serpro.gov.br/viodec-trial/v1/decode](https://gateway.apiserpro.serpro.gov.br/viodec-trial/v1/decode)

## 3) Como converter o QR Code de CNH para o arquivo `.bin`

### Requisitos

O código a seguir usa a biblioteca **ZXing** para ler o QR Code a partir de uma imagem PNG da CNH. Você pode encontrar a imagem na pasta `resources`.

Adicione as dependências no arquivo `pom.xml`:

```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.3</version>
</dependency>
```

### Código em Java

A seguir, o código Java para converter o QR Code de uma imagem PNG da CNH para o arquivo `.bin`:

```java
package com.mycompany.app;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class App {
    public static void main(String[] args) throws IOException, NotFoundException {

        String qrcodePath = "cnh-demo.png";
        String qrcodeContentPath = "teste.bin";
        
        InputStream is = App.class.getClassLoader().getResourceAsStream(qrcodePath);
        if (is == null) {
             throw new FileNotFoundException("Arquivo não encontrado!");
        }
        byte[] imageQRCode = is.readAllBytes();

        ByteArrayInputStream imageIS = new ByteArrayInputStream(imageQRCode);
        final BufferedImage bufferedImage = ImageIO.read(imageIS);
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);

        //Sobre HINTs https://zxing.github.io/zxing/apidocs/com/google/zxing/DecodeHintType.html
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        hints.put(DecodeHintType.CHARACTER_SET, Charset.forName("ISO-8859-1"));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        ArrayList<BarcodeFormat> formats = new ArrayList<BarcodeFormat>(1);
        formats.add(BarcodeFormat.QR_CODE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);

        // DECODE https://zxing.github.io/zxing/apidocs/com/google/zxing/MultiFormatReader.html
        MultiFormatReader qrCodeReader = new MultiFormatReader();
        qrCodeReader.setHints(hints);
        // Result https://zxing.github.io/zxing/apidocs/com/google/zxing/Result.html
        Result result = qrCodeReader.decodeWithState(bitmap);

        //Pegar os bytes do Result
        byte[] raw = result.getText().getBytes(Charset.forName("ISO-8859-1"));

        Files.write(Paths.get(qrcodeContentPath), raw);
    
    }
}

```

Este código lê a imagem do QR Code presente na pasta `resources`, decodifica os dados e os salva em um arquivo `.bin`.

## 4) Enviar o arquivo `.bin` para a API Vio Decoder

Depois de transformar a imagem em um arquivo `.bin`, use o seguinte comando **cURL** para enviar o arquivo para a API Vio Decoder e obter as informações sobre a CNH em formato JSON.

### Exemplo de cURL

```bash
curl --location --request POST 'https://gateway.apiserpro.serpro.gov.br/viodec-trial/v1/decode' \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/octet-stream' \
  --header 'Authorization: Bearer 06aef429-a981-3ec5-a1f8-71d38d86481e' \
  --data-binary '@cnh-demo.bin'
```

### Explicação dos parâmetros:
- `--location`: Segue qualquer redirecionamento (útil em APIs que redirecionam para outras URLs).
- `--header 'Accept: application/json'`: Solicita que a resposta seja no formato JSON.
- `--header 'Content-Type: application/octet-stream'`: Informa que o corpo da requisição contém dados binários.
- `--header 'Authorization: Bearer <TOKEN>'`: Substitua `<TOKEN>` pelo token de autenticação fornecido pela API.
- `--data-binary '@cnh-demo.bin'`: Envia o arquivo `.bin` como dados binários no corpo da requisição.

## 5) Exemplo de saída da requisição (Response JSON)

Após a execução do comando cURL, a API retorna um JSON com as informações contidas no QR Code da CNH.

### Exemplo de resposta JSON:

```json
{
    "template": {
        "name": "Demostração - CNH",
        "owner": {
            "name": "SERPRO"
        }
    },
    "data": {
        "nome": "JOSE DA SILVA SANTOS",
        "nome_civil": "JOSE DA SILVA SANTOS",
        "identidade": "00000000",
        "cpf": "00000000000",
        "data_nascimento": "12/01/1984",
        "filiacao_pai": "JOAO DA SILVA SANTOS",
        "filiacao_mae": "ROSA DA SILVA SANTOS",
        "permissao": "PERMISSAO",
        "categoria": "AB",
        "numero_registro": "00000000000",
        "data_validade": "12/01/2033",
        "data_primeira_habilitacao": "13/06/2008",
        "observacoes": "99",
        "local_emissao": "BRASILIA",
        "uf_emissao": "DF",
        "data_emissao": "12/01/2023",
        "numero_validacao_cnh": "00000000000",
        "numero_renach": "DF000000000"
    },
    "image": {
        "base64": "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",
        "type": "image/png"
    }
}
```

- O campo `data` contém os dados da CNH.
- O campo `image.base64` contém a foto da pessoa em formato Base64.

## 6) Converter Base64 para Imagem no Linux

No Linux, você pode converter o conteúdo Base64 para uma imagem (ou qualquer tipo de arquivo) usando o comando `base64` com a flag `-d` (decode). 

### Exemplo de comando para converter Base64 para imagem:

```bash
echo "iVBORw0KGgoAAA..." | base64 -d > face.png
```

Isso irá gerar uma imagem PNG chamada `face.png` a partir do conteúdo Base64 presente no campo `image.base64`.