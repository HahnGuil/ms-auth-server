# Regras de negócio

- **Conta por aplicação:** um e-mail pode ser usado em aplicações diferentes, mas não pode ser cadastrado duas vezes na mesma aplicação. Senhas são armazenadas por combinação usuário/aplicação, não são compartilhadas entre aplicações.
- **Tipos de conta:** usuários diretos entram com e-mail e senha. Usuários OAuth entram pelo Google e não podem usar login direto nem alterar senha neste serviço. Usuários OAuth são associados a uma aplicação pelo fluxo autenticado `/users/register`.
- **Senha:** formato exigido na API: 8–12 caracteres, com dígito, maiúscula e caractere especial. Senhas são armazenadas com BCrypt. Cada aplicação pode desabilitar expiração, usar a política padrão de 90 dias ou definir expiração em meses.
- **Expiração da senha:** aplicações com política recebem avisos quando faltam 20, 15, 10 e 5 dias. A senha expirada bloqueia o acesso daquela aplicação; redefini-la ou alterá-la remove o bloqueio dessa aplicação.
- **Sessão do usuário:** há uma sessão ativa por usuário e aplicação. Um novo login encerra e invalida a sessão anterior nessa aplicação, sem encerrar sessões em outras aplicações.
- **Tokens de usuário:** JWT de acesso dura 15 minutos e o refresh de usuário, 30 minutos. Refresh exige o token correto e ativo, e invalida o token anterior. Logout e alteração/redefinição de senha também invalidam a sessão correspondente.
- **Recuperação:** o código numérico de seis dígitos expira em 30 minutos; uma nova solicitação substitui a anterior para aquele usuário/aplicação. O `recoverToken` retornado após validar o código tem validade de 15 minutos e só serve para redefinir a senha.
- **Aplicações:** somente usuários `USER_ADMIN` ou `SUPER_USER`, com token de usuário ativo, podem cadastrar aplicações. A política padrão é 90 dias; a política customizada exige `passwordExpirationMonths >= 1`. `definePasswordPolicy=false` significa sem expiração.
- **Tokens de aplicação:** são separados dos tokens de usuário e têm escopos próprios. O token de acesso de aplicação dura 10 horas. O refresh de aplicação está configurado no código para 15 minutos (apesar do nome da constante indicar horas).
- **Perfis e papéis:** novo usuário recebe papel global normal e papel de aplicação `USER`. `PATCH /users` atualiza o papel de aplicação para `ADMIN` quando o usuário já está associado.
- **Chaves JWT:** tokens são assinados com RSA/RS256. As chaves são geradas na inicialização e rotacionadas a cada duas horas; as chaves públicas antigas permanecem publicadas enquanto o processo continua ativo. Uma reinicialização gera novo conjunto de chaves.

As durações e regras acima refletem a implementação atual; consulte o OpenAPI e os endpoints reais ao integrar versões futuras.
